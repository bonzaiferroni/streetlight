package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class EventFeedSelectors(
    val event: String?,
    val title: String?,
    val link: String?,
    val cost: String?,
    val description: String?,
    val requirements: String?,
    val time: String?,
)

@Serializable
data class EventPageSelectors(
    val title: String?,
    val link: String?,
    val cost: String?,
    val description: String?,
    val requirements: String?,
    val time: String?,
)

@Serializable
data class EventSelectorSchema(
    val feed: EventFeedSelectors,
    val page: EventPageSelectors?,
)