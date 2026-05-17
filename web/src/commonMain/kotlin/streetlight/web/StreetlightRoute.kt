package streetlight.web

import kampfire.api.StringId
import kampfire.api.TableId
import koala.html.AppRoute
import koala.html.AppScreen
import koala.html.IdOrNullParse
import koala.html.IdParse
import koala.html.RouteParse
import koala.html.StaticParse
import koala.html.UuidParse
import koala.model.DocId
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.PostId
import streetlight.model.data.Slug
import streetlight.model.data.SongId
import streetlight.model.data.TalentId
import streetlight.model.data.SpaceType
import kotlin.uuid.Uuid

enum class StreetlightScreen(
    override val pathRoot: String,
    override val routeParse: RouteParse,
): AppScreen {
    Home("", StaticParse { HomeRoute }),
    StarDash("account", StaticParse { StarDashRoute }),
    EventProfile("e", IdParse { EventSlugRoute(it) }),
    EditEvent("edit-event", UuidParse { EditEventIdRoute(EventId(it)) }),
    EditPost("edit-post", UuidParse { EditPostRoute(PostId(it)) }),
    EditLocation("edit-location", UuidParse { EditLocationIdRoute(LocationId(it)) }),
    Sandbox("sandbox", StaticParse { SandboxRoute }),
    Earth("earth", IdOrNullParse { EarthMapRoute(it) }),
    Chat("chat", StaticParse { ChatRoute }),
    SongProfile("song-profile", UuidParse { SongProfileRoute(SongId(it)) }),
    TalentProfile("talent-profile", UuidParse { TalentProfileRoute(TalentId(it)) }),
    EditTalent("edit-talent", UuidParse { EditTalentRoute(TalentId(it)) }),
    Location("location", UuidParse { LocationIdRoute(LocationId(it)) }),
    LocationAdmin("location-admin", UuidParse { LocationAdminRoute(LocationId(it)) }),

    // galaxy
    Galaxy("g", IdParse { GalaxyRoute(it) }),
    GalaxyFoundry("create-galaxy", StaticParse { GalaxyFoundryRoute }),
    GalaxyConfig("edit-galaxy", IdParse { GalaxyConfigRoute(it) } ),
    GalaxyList("galaxies", StaticParse { GalaxyListRoute }),

    Star("s", IdParse { StarRoute(it) }),
    EventScout("post-event", IdParse { EventScoutRoute(it) }),
    LocationScout("post-location", IdParse { LocationScoutRoute(it) }),
    PostContent("post-content", IdParse { PostContentRoute(it) }),
    Post("p", IdParse { StarPostRoute(it) }),
    EditStar("edit-profile", StaticParse { EditStarRoute }),
    SiteConfig("config", StaticParse { SiteConfigRoute }),
    AboutApp("about", StaticParse { AboutRoute }),
    SiteDoc("docs", IdParse { SiteDocRoute(it) }),
    Talk("talk", UuidParse { TalkRoute(GalaxyId(it)) })
}

sealed interface StreetlightRoute: AppRoute

interface ProjectIdRoute: StreetlightRoute {
    val id: TableId<Uuid>?

    override fun toSitePath() = toIdSitePath(id)
}

interface StringIdRoute: StreetlightRoute {
    val id: StringId

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

data class EventObjectRoute(val event: EventLocation): EventRoute, ProjectIdRoute {
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

data class EditEventIdRoute(val eventId: EventId? = null): EditEventRoute, ProjectIdRoute {
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

object ChatRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Chat
    override val title get() = "Chat"
}

data class SongProfileRoute(
    val songId: SongId
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.SongProfile
    override val id get() = songId
    override val title get() = "Song"
}

data class TalentProfileRoute(
    val talentId: TalentId
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.TalentProfile
    override val id get() = talentId
    override val title get() = "Talent"
}

data class EditTalentRoute(
    val talentId: TalentId? = null
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.EditTalent
    override val id get() = talentId
    override val title get() = "Talent"
}

data class LocationIdRoute(
    val locationId: LocationId
): StreetlightRoute, ProjectIdRoute {
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
): EditLocationRoute, ProjectIdRoute {
    override val id get() = locationId
}

data class EditLocationDataRoute(
    val location: LocationEdit
): EditLocationRoute, ProjectIdRoute {
    override val id get() = location.locationId
}

data class LocationAdminRoute(
    val locationId: LocationId
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.LocationAdmin
    override val id get() = locationId
    override val title get() = "Location Admin"
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

data class PostContentRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.PostContent
    override val title get() = "Post Content"
}

data class EditPostRoute(val postId: PostId): ProjectIdRoute {
    override val id get() = postId
    override val screen get() = StreetlightScreen.EditPost
    override val title get() = "Edit Post"
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

data class SiteDocRoute(val docId: DocId): StreetlightRoute {
    override val screen get() = StreetlightScreen.SiteDoc
    override val title get() = "Documentation"

    override fun toSitePath() = toIdSitePath(docId)
}

data class TalkRoute(val id: Uuid, val type: SpaceType): StreetlightRoute {
    constructor(galaxyId: GalaxyId): this(galaxyId.value, SpaceType.Galaxy)

    override val screen get() = StreetlightScreen.Talk
    override val title get() = "Talk"

    override fun toSitePath() = toIdSitePath(id)
}

data class StarPostRoute(override val id: StringId): StringIdRoute {
    override val screen get() = StreetlightScreen.Post
    override val title get() = "Post"
}

private fun AppRoute.toIdSitePath(id: String?) = id?.let { "$basePath/$id" } ?: basePath
private fun AppRoute.toIdSitePath(id: TableId<Uuid>?) = toIdSitePath(id?.value.toString())
private fun AppRoute.toIdSitePath(id: Uuid?) = toIdSitePath(id?.toString())
