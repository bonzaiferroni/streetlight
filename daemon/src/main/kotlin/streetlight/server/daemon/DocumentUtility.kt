package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Document.readCanonicalUrl(fetchedFrom: Url): Url? {
    val declared = canonicalCandidates()
        .firstNotNullOfOrNull { it.resolveAgainst(fetchedFrom) }
        ?: return null

    // A canonical pointing off-host is a redirect claim, not an identity claim.
    // Let the caller decide; we only vouch for same-host answers.
    return declared.takeIf { it.host != null && it.host == fetchedFrom.host }
}

private fun Document.canonicalCandidates(): Sequence<String> = sequence {
    // 1. The declared canonical link — the only element meant for this purpose
    yieldAll(select("link[rel~=(?i)^canonical$][href]").map { it.attr("href") })

    // 2. Open Graph — widely present, usually agrees with canonical
    yieldAll(select("meta[property=(?i)og:url][content]").map { it.attr("content") })

    // 3. Schema.org microdata on the page or body
    yieldAll(select("meta[itemprop=(?i)url][content]").map { it.attr("content") })
    yieldAll(select("[itemscope] > link[itemprop=(?i)url][href]").map { it.attr("href") })

    // 4. JSON-LD, where event sites often keep their best identifiers
    yieldAll(jsonLdUrls())
}

private fun String.resolveAgainst(base: Url): Url? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null

    val absolute = when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("//") -> base.value.substringBefore("://") + ":" + trimmed
        trimmed.startsWith("/") -> base.value.substringBefore("://") + "://" +
                base.value.substringAfter("://").substringBefore('/') + trimmed
        else -> return null  // relative canonicals are legal but rare and error-prone
    }
    return Url(absolute).normalize()
}

private fun Document.jsonLdUrls(): List<String> =
    select("script[type=(?i)application/ld+json]")
        .mapNotNull { runCatching { Json.parseToJsonElement(it.data()) }.getOrNull() }
        .flatMap { it.flattenGraph() }
        .mapNotNull { node ->
            node["url"]?.asStringOrNull() ?: node["@id"]?.asStringOrNull()
        }

private fun JsonElement.flattenGraph(): List<JsonObject> = when (this) {
    is JsonArray -> flatMap { it.flattenGraph() }
    is JsonObject -> (this["@graph"]?.flattenGraph() ?: emptyList()) + listOf(this)
    else -> emptyList()
}

private fun JsonElement.asStringOrNull(): String? =
    (this as? JsonPrimitive)?.takeIf { it.isString }?.content

data class StructuredDataReport(
    val jsonLdBlocks: Int,
    val jsonLdMalformed: Int,
    val types: List<String>,
    val eventCount: Int,
    val microdataEventCount: Int,
) {
    val hasEvents get() = eventCount > 0 || microdataEventCount > 0
}

private val eventTypes = setOf(
    "Event", "BusinessEvent", "ChildrensEvent", "ComedyEvent", "CourseInstance",
    "DanceEvent", "DeliveryEvent", "EducationEvent", "ExhibitionEvent", "Festival",
    "FoodEvent", "Hackathon", "LiteraryEvent", "MusicEvent", "PublicationEvent",
    "SaleEvent", "ScreeningEvent", "SocialEvent", "SportsEvent", "TheaterEvent",
    "VisualArtsEvent",
)

fun Document.readStructuredData(): StructuredDataReport {
    val scripts = select("script[type=(?i)application/ld+json]")

    var malformed = 0
    val nodes = scripts.flatMap { script ->
        val parsed = runCatching { Json.parseToJsonElement(script.data()) }.getOrNull()
        if (parsed == null) {
            malformed++
            emptyList()
        } else {
            parsed.flattenGraph()
        }
    }

    val types = nodes.flatMap { it.typeNames() }

    val microdataEvents = select("[itemtype]").count { element ->
        element.attr("itemtype").substringAfterLast('/') in eventTypes
    }

    return StructuredDataReport(
        jsonLdBlocks = scripts.size,
        jsonLdMalformed = malformed,
        types = types.distinct().sorted(),
        eventCount = types.count { it in eventTypes },
        microdataEventCount = microdataEvents,
    )
}

private fun JsonObject.typeNames(): List<String> = when (val type = this["@type"]) {
    is JsonPrimitive -> listOf(type.content)
    is JsonArray -> type.mapNotNull { (it as? JsonPrimitive)?.content }
    else -> emptyList()
}