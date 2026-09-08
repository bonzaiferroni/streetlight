package streetlight.model.data

import kampfire.model.GeoPoint
import koala.model.DocNode
import koala.model.DocTable
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class LocationContent(
    val location: Location,
    override val design: PageDesign?,
    val events: List<Event>,
    val canEdit: Boolean,
): RouteContent, DesignContent {
    override val geoPoint: GeoPoint get() = location.geoPoint
}

@Serializable
data class LocationUpdaterContent(
    val location: Location,
    val editLogs: List<EditLog>
): RouteContent

@Serializable
data class EventUpdaterContent(
    val event: Event,
    val editLogs: List<EditLog>
): RouteContent

@Serializable
data class GalaxyContent(
    val galaxy: Galaxy,
    val posts: List<FeedEntity>,
    val marks: Map<GalaxyId, List<Mark>>,
    val postMarks: Map<PostId, List<MarkStatus>>
): RouteContent, DesignContent {
    override val design get() = galaxy.design
    override val geoPoint get() = galaxy.geoPoint
}

@Serializable
data class StarContent(
    val star: Star,
    val posts: List<Media>,
    val isCaller: Boolean,
): RouteContent, DesignContent {
    override val geoPoint get() = null
    override val design get() = star.design
}

@Serializable
data class HomeContent(
    val galaxies: List<Galaxy>,
    val posts: List<FeedEntity>,
    val marks: Map<GalaxyId, List<Mark>>,
    val postMarks: Map<PostId, List<MarkStatus>>,
): RouteContent

@Serializable
data class DocContent(
    val node: DocNode,
    val table: DocTable,
): RouteContent