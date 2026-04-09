package streetlight.web

import kampfire.api.TableId
import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.PostId
import streetlight.model.data.Slug
import streetlight.model.data.SongId
import streetlight.model.data.TalentId

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> AppRoute?
): AppScreen {
    Home("home", { HomeRoute }),
    StarDash("account", { StarDashRoute }),
    EventProfile("e", { path -> path.provideRouteFromPath { EventSlugRoute(it) } }),
    EditEvent("edit-event", { path -> EditEventIdRoute(path.provideId { EventId(it) }) }),
    EditStory("edit-story", { path -> EditPostRoute(path.provideId { PostId(it) }) }),
    EditLocation("edit-location", { path -> EditLocationIdRoute(path.provideId { LocationId(it) }) }),
    Sandbox("sandbox", { SandboxRoute }),
    FullMap("full-map", { FullMapRoute }),
    Chat("chat", { ChatRoute }),
    SongProfile("song-profile", { path -> path.provideRouteFromPath { SongProfileRoute(SongId(it)) } }),
    TalentProfile("talent-profile", { path -> path.provideRouteFromPath { TalentProfileRoute(TalentId(it)) } }),
    EditTalent("edit-talent", { path -> EditTalentRoute(path.provideId { TalentId(it)} ) }),
    LocationProfile("location", { path -> path.provideRouteFromPath { LocationIdRoute(LocationId(it)) } }),
    LocationAdmin("location-admin", { path -> path.provideRouteFromPath { LocationAdminRoute(LocationId(it)) } }),
    ScoutMap("scout-map", { ScoutMapRoute }),
    CreateGalaxy("create-galaxy", { CreateGalaxyRoute }),
    GalaxyList("galaxies", { GalaxyListRoute }),
    GalaxyProfile("g", { path -> path.provideRouteFromPath { GalaxySlugRoute(it) }}),
    StarProfile("s", { path -> path.provideRouteFromPath { StarProfileRoute(it) } }),
    EventScout("post-event", { path -> path.provideRouteFromPath { EventScoutRoute(it) }}),
    LocationScout("post-location", { path -> path.provideRouteFromPath { LocationScoutRoute(it) } }),
    EditStar("edit-profile", { EditStarRoute }),
    SiteConfig("config", { SiteConfigRoute }),

}

fun List<String>.provideRouteFromPath(argIndex: Int = 1, provideRoute: (String) -> AppRoute?) =
    getOrNull(argIndex)?.let { provideRoute(it) }

fun <T: TableId<String>> List<String>.provideId(argIndex: Int = 1, provideId: (String) -> T) =
    getOrNull(argIndex)?.let { provideId(it) }

sealed interface StreetlightRoute: AppRoute

sealed interface StringIdRoute: StreetlightRoute {
    val id: TableId<String>?

    override fun toHashPath() = id?.let { "${super.toHashPath()}/${it.value}" } ?: super.toHashPath()
}

sealed interface SlugRoute: StreetlightRoute {
    val slug: Slug?
    override fun toHashPath() = slug?.let { "${super.toHashPath()}/${it}" } ?: super.toHashPath()
}

object HomeRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Home
    override val title get() = "Home"
}

object StarDashRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.StarDash
    override val title get() = "You"
}

sealed interface EventRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.EventProfile
    override val title get() = "Event"
}

data class EventObjectRoute(val event: EventLocation): EventRoute, StringIdRoute {
    override val id get() = event.eventId
    override val title get() = event.title
}

sealed interface EditEventRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.EditEvent
    override val title get() = "Post Event"
}

data class EditEventCallbackRoute(
    val event: EventEdit,
    val callback: (Event?) -> Unit
): EditEventRoute

data class EditEventIdRoute(val eventId: EventId? = null): EditEventRoute, StringIdRoute {
    override val id get() = eventId
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Sandbox
    override val title get() = "Sandbox"
}

object FullMapRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.FullMap
    override val title get() = "Map"
}

data class EditPostRoute(
    val postId: PostId? = null
): StreetlightRoute {
    override val screen get() = StreetlightScreen.EditStory
    override val title get() = "Share Post"
}

object ChatRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Chat
    override val title get() = "Chat"
}

data class SongProfileRoute(
    val songId: SongId
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.SongProfile
    override val id get() = songId
    override val title get() = "Song"
}

data class TalentProfileRoute(
    val talentId: TalentId
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.TalentProfile
    override val id get() = talentId
    override val title get() = "Talent"
}

data class EditTalentRoute(
    val talentId: TalentId? = null
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.EditTalent
    override val id get() = talentId
    override val title get() = "Talent"
}

data class LocationIdRoute(
    val locationId: LocationId
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.LocationProfile
    override val id get() = locationId
    override val title get() = "Location"
}

sealed interface EditLocationRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.EditLocation
    override val title get() = "Share Location"
}

object CreateLocationRoute: EditLocationRoute

data class EditLocationIdRoute(
    val locationId: LocationId? = null
): EditLocationRoute, StringIdRoute {
    override val id get() = locationId
}

data class EditLocationDataRoute(
    val location: LocationEdit
): EditLocationRoute, StringIdRoute {
    override val id get() = location.locationId
}

data class LocationAdminRoute(
    val locationId: LocationId
): StreetlightRoute, StringIdRoute {
    override val screen get() = StreetlightScreen.LocationAdmin
    override val id get() = locationId
    override val title get() = "Location Admin"
}

object ScoutMapRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.ScoutMap
    override val title get() = "Scout Map"
}

object CreateGalaxyRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.CreateGalaxy
    override val title get() = "Create Galaxy"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyList
    override val title get() = "Galaxies"
}

data class GalaxySlugRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.GalaxyProfile
    override val title get() = "Galaxy"
}

data class EventSlugRoute(override val slug: Slug): StreetlightRoute, SlugRoute, EventRoute {
}

data class StarProfileRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.StarProfile
    override val title get() = "Star"
}

data class EventScoutRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.EventScout
    override val title get() = "Event Scout"
}

data class LocationScoutRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.LocationScout
    override val title get() = "Location Scout"
}

object EditStarRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.EditStar
    override val title get() = "Edit Profile"
}

object SiteConfigRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.SiteConfig
    override val title get() = "Config"
}