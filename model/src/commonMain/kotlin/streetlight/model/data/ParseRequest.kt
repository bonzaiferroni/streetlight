package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** A request to read a record from a page. */
@Serializable
sealed interface ParseRequest {
    val url: Url
}

/** A request to read the page at a URL. */
@Serializable
data class UrlParseRequest(
    override val url: Url
): ParseRequest

/** A request to read a page from its HTML, already fetched. */
@Serializable
data class HtmlParseRequest(
    override val url: Url,
    val html: String,
): ParseRequest

/** A request to read a record from an image, such as a flyer. */
@Serializable
data class ImageParseRequest(
    override val url: Url,
): ParseRequest

/** The events read from a page, as edits. */
@Serializable
data class MultiEventParseResponse(
    val hasContent: Boolean? = null,
    val events: List<EventEdit>? = null,
)

/** The details of a location as the LM reads them from its homepage. */
@Serializable
data class LocationRead(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val hours: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val url: String? = null,
    val eventsUrl: String? = null,
    val aboutUrl: String? = null,
    val menuUrl: String? = null,
    val imageUrl: String? = null,
) : LmSchema

/**
 * The details of an event as the LM reads them from its page, with the place it happens at. Dates and times are the
 * text our own parsers read: an ISO date and 24-hour times.
 */
@Serializable
data class EventRead(
    val name: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val ageMin: String? = null,
    val cost: String? = null,
    val contact: String? = null,
    val url: String? = null,
    val locationName: String? = null,
    val locationAddress: String? = null,
    val locationCity: String? = null,
    val locationState: String? = null,
    val locationPostalCode: String? = null,
    val locationWebsite: String? = null,
) : LmSchema
