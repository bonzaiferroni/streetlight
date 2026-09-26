package streetlight.agent

import kotlinx.serialization.Serializable
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema

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
        event = event, feedLocation = feedLocation, address = address, title = title,
        eventLocation = eventLocation, link = link, image = image, cost = cost,
        description = description, date = date, time = time,
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
        title = title, location = location, address = address, image = image, cost = cost,
        description = description, date = date, startTime = startTime, endTime = endTime,
        ageMin = ageMin, contact = contact,
    )
}

/** The selectors asked of the LM for the parts of an event's start, each holding one part alone. */
@Serializable
data class EventTimeSchemaRequest(
    val month: String? = null,
    val day: String? = null,
    val startTime: String? = null,
)
