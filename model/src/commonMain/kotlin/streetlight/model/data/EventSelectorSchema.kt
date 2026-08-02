package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class EventSelectorSchema(
    val feed: EventFeedSelectors,
    val page: EventPageSelectors?,
)

@Serializable
data class EventFeedSelectors(
    val event: String? = null,
    val title: String? = null,
    val link: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null,
)

@Serializable
data class EventPageSelectors(
    val title: String? = null,
    val image: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val ageMin: String? = null,
    val contact: String? = null,
)

// td: implement requirements
// val requirements: String?,