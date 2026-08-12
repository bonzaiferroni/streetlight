package streetlight.model.data

import koala.model.DocNode
import koala.model.DocTable
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class LocationContent(
    val location: Location,
    val design: PageDesign?,
    val events: List<Event>,
    val canEdit: Boolean,
): RouteContent

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
    val posts: List<GalaxyPost>,
): RouteContent

@Serializable
data class StarContent(
    val star: Star,
    val posts: List<Media>,
    val isCaller: Boolean,
): RouteContent

@Serializable
data class HomeContent(
    val galaxies: List<Galaxy>,
    val posts: List<GalaxyPost>
): RouteContent

@Serializable
data class DocContent(
    val node: DocNode,
    val table: DocTable,
): RouteContent