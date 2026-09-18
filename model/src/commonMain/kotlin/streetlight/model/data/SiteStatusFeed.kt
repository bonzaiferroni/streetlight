package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SiteStatusFeed(
    val points: List<SiteStatus>,
    val events: List<SiteEvent>,
)