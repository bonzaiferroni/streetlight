package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SiteStatusFeed(
    val points: List<StatusStatus>,
    val events: List<SiteEvent>,
)