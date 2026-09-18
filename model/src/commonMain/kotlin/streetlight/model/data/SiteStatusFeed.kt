package streetlight.model.data

data class SiteStatusFeed(
    val points: List<StatusPoint>,
    val events: List<SiteEvent>,
)