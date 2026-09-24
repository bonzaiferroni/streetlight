package streetlight.model.data

import kotlinx.serialization.Serializable

/** The status of the site over time, with its marked events. */
@Serializable
data class SiteStatusFeed(
    val points: List<SiteStatus>,
    val events: List<SiteEvent>,
)