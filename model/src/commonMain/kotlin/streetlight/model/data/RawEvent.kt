package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** An event as read from a page: the text of each field, not yet parsed. */
@Serializable
data class RawEvent(
    val title: String? = null,
    val url: Url? = null,
    val image: String? = null,
    val descriptionHtml: String? = null,
    val contact: String? = null,
    val cost: String? = null,
    val ageMin: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
    val address: String? = null,
)
