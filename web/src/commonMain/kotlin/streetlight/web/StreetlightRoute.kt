package streetlight.web

import kampfire.api.StringId
import kampfire.api.TableId
import koala.html.AppRoute
import koala.html.AppScreen
import koala.html.ScreenParameter
import koala.model.DocId
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.ProtoPostId
import streetlight.model.data.Slug
import streetlight.model.data.SongId
import streetlight.model.data.TalentId
import streetlight.model.data.SpaceType

enum class StreetlightScreen(
    override val pathRoot: String,
    override val provideRoute: (List<String>) -> AppRoute?,
    override val parameter: ScreenParameter? = null
): AppScreen {
    Home("", { HomeRoute }),
    StarDash("account", { StarDashRoute }),
    EventProfile("e", { path -> path.provideRouteFromPath { EventSlugRoute(it) } }, ScreenParameter.Slug),
    EditEvent("edit-event", { path -> EditEventIdRoute(path.provideId { EventId(it) }) }, ScreenParameter.Id),
    EditStory("edit-story", { path -> EditPostRoute(path.provideId { ProtoPostId(it) }) }, ScreenParameter.Id),
    EditLocation("edit-location", { path -> EditLocationIdRoute(path.provideId { LocationId(it) }) }, ScreenParameter.Id),
    Sandbox("sandbox", { SandboxRoute }),
    Earth("earth", { path -> EarthMapRoute(path.getOrNull(1))}, ScreenParameter.Slug),
    Chat("chat", { ChatRoute }),
    SongProfile("song-profile", { path -> path.provideRouteFromPath { SongProfileRoute(SongId(it)) } }, ScreenParameter.Id),
    TalentProfile("talent-profile", { path -> path.provideRouteFromPath { TalentProfileRoute(TalentId(it)) } }, ScreenParameter.Id),
    EditTalent("edit-talent", { path -> EditTalentRoute(path.provideId { TalentId(it)} ) }, ScreenParameter.Id),
    Location("location", { path -> path.provideRouteFromPath { LocationIdRoute(LocationId(it)) } }, ScreenParameter.Id),
    LocationAdmin("location-admin", { path -> path.provideRouteFromPath { LocationAdminRoute(LocationId(it)) } }, ScreenParameter.Id),
    CreateGalaxy("create-galaxy", { CreateGalaxyRoute }),
    GalaxyList("galaxies", { GalaxyListRoute }),
    Galaxy("g", { path -> path.provideRouteFromPath { GalaxySlugRoute(it) }}, ScreenParameter.Slug),
    Star("s", { path -> path.provideRouteFromPath { StarRoute(it) } }, ScreenParameter.Slug),
    EventScout("post-event", { path -> path.provideRouteFromPath { EventScoutRoute(it) }}, ScreenParameter.Slug),
    LocationScout("post-location", { path -> path.provideRouteFromPath { LocationScoutRoute(it) } }, ScreenParameter.Slug),
    EditStar("edit-profile", { EditStarRoute }),
    SiteConfig("config", { SiteConfigRoute }),
    AboutApp("about", { AboutRoute }),
    PrivacyPolicy("privacy", { PrivacyPolicyRoute }),
    SiteDoc("docs", { path -> path.provideRouteFromPath { SiteDocRoute(it) } }, ScreenParameter.Slug),
    Talk("talk", { path -> path.provideRouteFromPath { TalkRoute(GalaxyId(it)) }}, ScreenParameter.Slug)
}

fun List<String>.provideRouteFromPath(argIndex: Int = 1, provideRoute: (String) -> AppRoute?) =
    getOrNull(argIndex)?.let { provideRoute(it) }

fun <T: TableId<String>> List<String>.provideId(argIndex: Int = 1, provideId: (String) -> T) =
    getOrNull(argIndex)?.let { provideId(it) }

sealed interface StreetlightRoute: AppRoute

sealed interface StringIdRoute: StreetlightRoute {
    val id: TableId<String>?

    override fun toSitePath() = toIdSitePath(id)
}

sealed interface SlugRoute: StreetlightRoute {
    val slug: Slug?
    override fun toSitePath() = toIdSitePath(slug)
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

data class EarthMapRoute(val galaxySlug: String?): StreetlightRoute {
    override val screen get() = StreetlightScreen.Earth
    override val title get() = "Earth"

    override fun toSitePath() = toIdSitePath(galaxySlug)
}

data class EditPostRoute(
    val postId: ProtoPostId? = null
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
    override val screen get() = StreetlightScreen.Location
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

object CreateGalaxyRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.CreateGalaxy
    override val title get() = "Create Galaxy"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyList
    override val title get() = "Galaxies"
}

data class GalaxySlugRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.Galaxy
    override val title get() = "Galaxy"
}

data class EventSlugRoute(override val slug: Slug): StreetlightRoute, SlugRoute, EventRoute {
}

data class StarRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.Star
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

object AboutRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.AboutApp
    override val title get() = "About"
}

object PrivacyPolicyRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.PrivacyPolicy
    override val title get() = "Privacy Policy"
}

data class SiteDocRoute(val docId: DocId): StreetlightRoute {
    override val screen get() = StreetlightScreen.SiteDoc
    override val title get() = "Documentation"

    override fun toSitePath() = toIdSitePath(docId)
}

data class TalkRoute(val stringId: StringId, val type: SpaceType): StreetlightRoute {
    constructor(galaxyId: GalaxyId): this(galaxyId.value, SpaceType.Galaxy)

    override val screen get() = StreetlightScreen.Talk
    override val title get() = "Talk"

    override fun toSitePath() = toIdSitePath(stringId)
}

private fun AppRoute.toIdSitePath(id: String?) = id?.let { "$basePath/$id" } ?: basePath
private fun AppRoute.toIdSitePath(id: TableId<String>?) = toIdSitePath(id?.value)
