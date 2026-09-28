package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private fun JsonElement.flattenGraph(): List<JsonObject> = when (this) {
    is JsonArray -> flatMap { it.flattenGraph() }
    is JsonObject -> (this["@graph"]?.flattenGraph() ?: emptyList()) + listOf(this)
    else -> emptyList()
}

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