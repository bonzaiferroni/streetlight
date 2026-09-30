package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kampfire.model.normalize
import kampfire.model.toUrl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap

/**
 * An event as a page declares it in its JSON-LD, in schema.org's terms. Its [performers] are read, ready for when the
 * model holds them.
 */
data class LdEvent(
    val name: String?,
    val url: String?,
    val startDate: String?,
    val endDate: String?,
    val image: String?,
    val description: String?,
    val status: String?,
    val price: String?,
    val currency: String?,
    val tickets: String?,
    val performers: List<String>,
    val place: LdPlace?,
)

/** A place as a page declares it in its JSON-LD: a venue, a business or an organization with an address. */
data class LdPlace(
    val name: String?,
    val url: String?,
    val telephone: String?,
    val email: String?,
    val street: String?,
    val locality: String?,
    val region: String?,
    val postalCode: String?,
    val country: String?,
) {
    /** The city, state and postal code of this place, joined for a map search, or null when it gives none. */
    val area get() = listOfNotNull(locality, region, postalCode).joinToString(" ").ifEmpty { null }
}

/** The events the JSON-LD of this page declares, at any depth. */
fun Document.readLdEvents(): List<LdEvent> = readLdObjects()
    .filter { node -> node.typeNames().any { it in eventTypes } }
    .map { it.toLdEvent() }

/** The places the JSON-LD of this page declares on their own, not as the location of an event. */
fun Document.readLdPlaces(): List<LdPlace> {
    val objects = readLdObjects()
    val eventPlaces = objects.filter { node -> node.typeNames().any { it in eventTypes } }
        .mapNotNull { it["location"] as? JsonObject }.toSet()
    return objects
        .filter { it !in eventPlaces && it.typeNames().none { type -> type in eventTypes || type in notPlaceTypes } }
        .filter { it["address"] is JsonObject && it.text("name") != null }
        .map { it.toLdPlace() }
}

/**
 * The event of the page at [pageUrl] among [events]: the one whose url is the page's, or the only one. Null when
 * there are several and none is the page's.
 */
fun pageLdEvent(events: List<LdEvent>, pageUrl: Url): LdEvent? {
    val page = pageUrl.normalize()
    return events.firstOrNull { event -> event.url?.toUrl()?.takeIf { it.isAbsolute }?.normalize() == page }
        ?: events.singleOrNull()
}

/** The event the JSON-LD of this page at [pageUrl] declares as its own, picked as [pageLdEvent] picks it. */
fun Document.readPageLdEvent(pageUrl: Url): LdEvent? = pageLdEvent(readLdEvents(), pageUrl)

/** Whether this event was called off or moved, so it is not to be posted as it stands. */
val LdEvent.isCalledOff get() = status?.substringAfterLast('/') in calledOffStatuses

/**
 * The values this event declares, each under its [ParseProperty], to lay over what the page was read for. Its start is
 * the local time the page states, whatever offset it carries, and its end only on the start's date. Its description
 * is a [ParseProperty.DeclaredDescription].
 */
fun LdEvent.toPropertyMap(): PropertyMap {
    val (date, startTime) = startDate?.let { splitDateTime(it) } ?: (null to null)
    val (endDate, endTime) = endDate?.let { splitDateTime(it) } ?: (null to null)
    return listOf(
        ParseProperty.Name to name,
        ParseProperty.Date to date,
        ParseProperty.StartTime to startTime,
        ParseProperty.EndTime to endTime?.takeIf { endDate == date },
        ParseProperty.Image to image,
        ParseProperty.DeclaredDescription to description,
        ParseProperty.Cost to price?.let { if (it.toFloatOrNull() == 0f) "Free" else "$$it" }
            ?.takeIf { currency == null || currency == "USD" },
        ParseProperty.Tickets to tickets,
        ParseProperty.Location to place?.name,
        ParseProperty.Address to place?.street,
        ParseProperty.Area to place?.area,
    ).mapNotNull { (property, text) -> text?.let { property to it } }.toMap()
}

/** The values this place declares, each under its [ParseProperty], to lay over what its homepage was read for. */
fun LdPlace.toPropertyMap(): PropertyMap = listOf(
    ParseProperty.Name to name,
    ParseProperty.Address to street,
    ParseProperty.Phone to telephone,
    ParseProperty.Email to email,
).mapNotNull { (property, text) -> text?.let { property to it } }.toMap()

/** The one place the JSON-LD of this page declares on its own, or null when it declares none or several. */
fun Document.readPageLdPlace(): LdPlace? = readLdPlaces().distinctBy { it.name }.singleOrNull()

/** The date and the time of day of an ISO date-time as its page states it, the time null for a date alone. */
private fun splitDateTime(value: String): Pair<String, String?> {
    val date = value.take(10)
    val time = value.drop(11).take(5).takeIf { value.getOrNull(10) == 'T' && timeOfDay.matches(it) }
    return date to time
}

/** Plain [text] as html, each run of lines a paragraph; text already holding html is kept as it is. */
private fun proseHtml(text: String): String {
    if (htmlTag.containsMatchIn(text)) return text
    return text.split(blankLine).map { it.trim() }.filter { it.isNotEmpty() }
        .joinToString("") { "<p>${it.replace("\n", "<br>")}</p>" }
}

/** Every JSON object in the JSON-LD blocks of this page, at any depth. */
private fun Document.readLdObjects(): List<JsonObject> =
    select("script[type=application/ld+json]")
        .mapNotNull { runCatching { Json.parseToJsonElement(it.data()) }.getOrNull() }
        .flatMap { it.allObjects() }

private fun JsonElement.allObjects(): List<JsonObject> = when (this) {
    is JsonArray -> flatMap { it.allObjects() }
    is JsonObject -> listOf(this) + values.flatMap { it.allObjects() }
    else -> emptyList()
}

private fun JsonObject.toLdEvent(): LdEvent {
    val offer = this["offers"].let { (it as? JsonArray)?.firstOrNull() ?: it } as? JsonObject
    val place = this["location"].let { location ->
        (location as? JsonArray)?.firstOrNull { (it as? JsonObject)?.typeNames()?.contains("VirtualLocation") == false }
            ?: location
    } as? JsonObject
    return LdEvent(
        name = text("name"),
        url = text("url"),
        startDate = text("startDate"),
        endDate = text("endDate"),
        image = imageOf(this["image"]),
        description = text("description")?.let { proseHtml(it) },
        status = text("eventStatus"),
        price = offer?.text("price") ?: offer?.text("lowPrice"),
        currency = offer?.text("priceCurrency"),
        tickets = offer?.text("url"),
        performers = this["performer"].let { it as? JsonArray ?: listOfNotNull(it) }
            .mapNotNull { (it as? JsonObject)?.text("name") ?: (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content },
        place = place?.toLdPlace(),
    )
}

private fun JsonObject.toLdPlace(): LdPlace {
    val address = this["address"] as? JsonObject
    return LdPlace(
        name = text("name"),
        url = text("url") ?: text("sameAs"),
        telephone = text("telephone"),
        email = text("email")?.removePrefix("mailto:"),
        street = address?.text("streetAddress"),
        locality = address?.text("addressLocality"),
        region = address?.text("addressRegion"),
        postalCode = address?.text("postalCode"),
        country = (address?.get("addressCountry") as? JsonObject)?.text("name") ?: address?.text("addressCountry"),
    )
}

/** The text of the property [key], or null when it is missing, blank or not text. */
private fun JsonObject.text(key: String): String? =
    ((this[key] as? JsonPrimitive)?.takeIf { it.isString || it.content.toDoubleOrNull() != null })?.content
        ?.trim()?.takeIf { it.isNotEmpty() }

private fun imageOf(element: JsonElement?): String? = when (element) {
    is JsonPrimitive -> element.content.takeIf { element.isString }
    is JsonArray -> element.firstNotNullOfOrNull { imageOf(it) }
    is JsonObject -> element.text("url")
    else -> null
}

private fun JsonObject.typeNames(): List<String> = when (val type = this["@type"]) {
    is JsonPrimitive -> listOf(type.content)
    is JsonArray -> type.mapNotNull { (it as? JsonPrimitive)?.content }
    else -> emptyList()
}

private val eventTypes = setOf(
    "Event", "BusinessEvent", "ChildrensEvent", "ComedyEvent", "CourseInstance",
    "DanceEvent", "DeliveryEvent", "EducationEvent", "ExhibitionEvent", "Festival",
    "FoodEvent", "Hackathon", "LiteraryEvent", "MusicEvent", "PublicationEvent",
    "SaleEvent", "ScreeningEvent", "SocialEvent", "SportsEvent", "TheaterEvent",
    "VisualArtsEvent",
)

private val notPlaceTypes = setOf("PostalAddress", "Offer", "WebSite", "WebPage", "ImageObject", "Person")

private val calledOffStatuses = setOf("EventCancelled", "EventPostponed")

private val timeOfDay = Regex("""\d{2}:\d{2}""")

private val htmlTag = Regex("""</?[a-zA-Z][^>]*>""")

private val blankLine = Regex("""\n\s*\n""")
