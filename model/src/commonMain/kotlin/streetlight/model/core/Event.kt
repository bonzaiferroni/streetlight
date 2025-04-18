package streetlight.model.core

import kotlinx.serialization.Serializable
import streetlight.model.enums.EventStatus

@Serializable
data class Event(
    override val id: Int = 0,
    val locationId: Int = 0,
    val userId: Long = 0,
    val timeStart: Long = 0L,
    val hours: Float? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    val streamUrl: String? = null,
    val name: String? = null, //
    val description: String? = null, //
    val status: EventStatus = EventStatus.Pending,
    val currentRequestId: Int? = null,
    val cashTips: Float? = null,
    val cardTips: Float? = null,
) : IdModel