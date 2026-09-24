package streetlight.model.data

import kampfire.model.GeoPoint
import koala.model.DocNode
import koala.model.DocTable
import koala.model.RouteContent
import kotlinx.serialization.Serializable

/** The content of a location's page: the location, its design, its events, and whether the caller may edit it. */
@Serializable
data class LocationContent(
    val location: Location,
    override val design: PageDesign?,
    val events: List<Event>,
    val canEdit: Boolean,
): RouteContent, DesignContent {
    override val geoPoint: GeoPoint get() = location.geoPoint
}

/** The content of a location's edit page: the location and its edit history. */
@Serializable
data class LocationUpdaterContent(
    val location: Location,
    val editLogs: List<EditLog>
): RouteContent

/** The content of an event's edit page: the event and its edit history. */
@Serializable
data class EventUpdaterContent(
    val event: Event,
    val editLogs: List<EditLog>
): RouteContent

/** The content of a galaxy's page: the galaxy and its feed. */
@Serializable
data class GalaxyContent(
    val galaxy: Galaxy,
    val feed: EntityFeed,
): RouteContent, DesignContent {
    override val design get() = galaxy.design
    override val geoPoint get() = galaxy.geoPoint
}

/** The content of a user's page: the user, their feed, and whether it is the caller's own. */
@Serializable
data class StarContent(
    val star: Star,
    val feed: EntityFeed,
    val isCaller: Boolean,
): RouteContent, DesignContent {
    override val geoPoint get() = null
    override val design get() = star.design
}

/** The content of the home page: the top galaxies and the universe feed. */
@Serializable
data class HomeContent(
    val galaxies: List<Galaxy>,
    val feed: EntityFeed,
): RouteContent

/** The content of a city's page: the city and its feed of locations and events. */
@Serializable
data class CityContent(
    val city: City,
    val feed: EntityFeed,
): RouteContent

/** The content of the city list. */
@Serializable
data class CityListContent(
    val cities: List<City>,
): RouteContent

/** The content of a doc page: the doc and the table of contents. */
@Serializable
data class DocContent(
    val node: DocNode,
    val table: DocTable,
): RouteContent