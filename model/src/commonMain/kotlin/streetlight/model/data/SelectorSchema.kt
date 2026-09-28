package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** The CSS selectors that pick the parts of a record out of a page. */
@Serializable
sealed interface SelectorSchema {
    val schemaType: SchemaType
}

/** The selectors of a page listing events. */
@Serializable
data class EventFeedSchema(
    val event: String? = null,
    val feedLocation: String? = null,
    val address: String? = null,
    val title: String? = null,
    val eventLocation: String? = null,
    val link: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null,
    val month: String? = null,
    val day: String? = null,
): SelectorSchema {
    override val schemaType get() = SchemaType.EventFeed
}

/** The selectors of the page of one event. */
@Serializable
data class EventPageSchema(
    val title: String? = null,
    val location: String? = null,
    val address: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val ageMin: String? = null,
    val contact: String? = null,
    val month: String? = null,
    val day: String? = null,
): SelectorSchema {
    override val schemaType get() = SchemaType.EventPage
}

/** The selectors of the homepage of a location. [socialLinks] matches a list of elements; each other selector, one. */
@Serializable
data class LocationSchema(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val hours: String? = null,
    val eventsLink: String? = null,
    val image: String? = null,
    val socialLinks: String? = null,
): SelectorSchema {
    override val schemaType get() = SchemaType.Location
}

/** The selector schemas of a location's page at [url]. */
@Serializable
data class UrlSchemas(
    val locationId: LocationId,
    val url: Url,
    val schemas: List<SelectorSchema>
)