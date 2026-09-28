package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema
import streetlight.model.data.LocationSelectorSchema

/** The selectors asked of the LM for a page listing events. */
@Serializable
data class EventFeedSchemaRequest(
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
) {
    fun toSchema() = EventFeedSchema(
        event = event.selectorOrNull(),
        feedLocation = feedLocation.selectorOrNull(),
        address = address.selectorOrNull(),
        title = title.selectorOrNull(),
        eventLocation = eventLocation.selectorOrNull(),
        link = link.selectorOrNull(),
        image = image.selectorOrNull(),
        cost = cost.selectorOrNull(),
        description = description.selectorOrNull(),
        date = date.selectorOrNull(),
        time = time.selectorOrNull(),
    )
}

/** The selectors asked of the LM for the page of one event. */
@Serializable
data class EventPageSchemaRequest(
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
) {
    fun toSchema() = EventPageSchema(
        title = title.selectorOrNull(),
        location = location.selectorOrNull(),
        address = address.selectorOrNull(),
        image = image.selectorOrNull(),
        cost = cost.selectorOrNull(),
        description = description.selectorOrNull(),
        date = date.selectorOrNull(),
        startTime = startTime.selectorOrNull(),
        endTime = endTime.selectorOrNull(),
        ageMin = ageMin.selectorOrNull(),
        contact = contact.selectorOrNull(),
    )
}

/** The selectors asked of the LM for the homepage of a location. */
@Serializable
data class LocationSchemaRequest(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val hours: String? = null,
    val eventsLink: String? = null,
    val image: String? = null,
    val socialLinks: String? = null,
) {
    fun toSchema() = LocationSelectorSchema(
        name = name.selectorOrNull(),
        description = description.selectorOrNull(),
        address = address.selectorOrNull(),
        phone = phone.selectorOrNull(),
        email = email.selectorOrNull(),
        hours = hours.selectorOrNull(),
        eventsLink = eventsLink.selectorOrNull(),
        image = image.selectorOrNull(),
        socialLinks = socialLinks.selectorOrNull(),
    )
}

/** The selectors asked of the LM for the parts of an event's start, each holding one part alone. */
@Serializable
data class EventTimeSchemaRequest(
    val month: String? = null,
    val day: String? = null,
    val startTime: String? = null,
)

/** The selector asked of the LM for the prose that describes a page's event. */
@Serializable
data class EventDescriptionSchemaRequest(
    val description: String? = null,
)

/** This selector, or `null` when the LM wrote a null as text, such as "null" or "none". */
fun String?.selectorOrNull(): String? = this?.takeUnless { it.trim().lowercase() in nullWords }

private val nullWords = setOf("", "null", "none", "n/a")
