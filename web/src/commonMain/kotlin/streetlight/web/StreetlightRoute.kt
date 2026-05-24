package streetlight.web

import kampfire.api.Slug
import kampfire.api.StringId
import kampfire.api.TableId
import kampfire.utils.pascalToKebabCase
import koala.html.AppRoute
import koala.html.AppScreen
import koala.html.SlugOrNullParse
import koala.html.IdParse
import koala.html.RouteParse
import koala.html.SlugParse
import koala.html.StaticParse
import koala.html.UuidParse
import koala.model.DocId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId
import streetlight.model.data.ProjectId
import streetlight.model.data.SongId
import streetlight.model.data.TalentId
import streetlight.model.data.SpaceType
import kotlin.uuid.Uuid

enum class StreetlightScreen(
    override val routeParse: RouteParse,
    pathRoot: String? = null,
): AppScreen {
    Home(StaticParse { HomeRoute }, ""),
    Account(StaticParse { StarDashRoute }),
    UpdatePost(SlugParse { PostUpdateRoute(it) }),
    Sandbox(StaticParse { SandboxRoute }),
    Earth(SlugOrNullParse { EarthMapRoute(it) }),
    Chat(StaticParse { ChatRoute }),
    SongProfile(UuidParse { SongProfileRoute(SongId(it)) }),
    TalentProfile(UuidParse { TalentProfileRoute(TalentId(it)) }),
    EditTalent(UuidParse { EditTalentRoute(TalentId(it)) }),

    // location
    Location(SlugParse { LocationRoute(it) }),
    LocationAdmin(UuidParse { LocationAdminRoute(LocationId(it)) }),
    LocationScout(SlugParse { LocationScoutRoute(it) }),
    LocationUpdate(SlugParse { UpdateLocationRoute(it) }),

    // galaxy
    Galaxy(SlugParse { GalaxyRoute(it) }, "g"),
    GalaxyFoundry(StaticParse { GalaxyFoundryRoute }),
    GalaxyUpdate(SlugParse { GalaxyConfigRoute(it) } ),
    Galaxies(StaticParse { GalaxyListRoute }),

    // event
    Event(SlugParse { EventRoute(it) }, "e"),
    UpdateEvent(SlugParse { EventUpdateRoute(it) }),

    Star(SlugParse { StarRoute(it) }, "s"),
    EventScout(SlugParse { EventScoutRoute(it) }),
    CreatePost(SlugParse { CreatePostRoute(it) }),
    Post(SlugParse { PostRoute(it) }, "p"),
    EditStar(StaticParse { EditStarRoute }),
    SiteConfig(StaticParse { SiteConfigRoute }),
    AboutApp(StaticParse { AboutRoute }),
    Docs(IdParse { SiteDocRoute(it) }),
    Talk(UuidParse { TalkRoute(GalaxyId(it)) });

    override val pathRoot = pathRoot ?: name.pascalToKebabCase()
}

sealed interface StreetlightRoute: AppRoute

interface ProjectIdRoute: StreetlightRoute {
    val projectId: ProjectId?

    override fun toSitePath() = toIdSitePath(projectId)
}

interface SlugOrIdRoute: StreetlightRoute {
    val value: StringId

    override fun toSitePath() = toIdSitePath(value)
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
    override val screen get() = StreetlightScreen.Account
    override val title get() = "You"
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Sandbox
    override val title get() = "Sandbox"
}

data class EarthMapRoute(override val slug: Slug?): SlugRoute {
    override val screen get() = StreetlightScreen.Earth
    override val title get() = "Earth"

    override fun toSitePath() = toIdSitePath(slug)
}

object ChatRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Chat
    override val title get() = "Chat"
}

data class SongProfileRoute(
    val songId: SongId
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.SongProfile
    override val projectId get() = songId
    override val title get() = "Song"
}

data class TalentProfileRoute(
    val talentId: TalentId
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.TalentProfile
    override val projectId get() = talentId
    override val title get() = "Talent"
}

data class EditTalentRoute(
    val talentId: TalentId? = null
): StreetlightRoute, ProjectIdRoute {
    override val screen get() = StreetlightScreen.EditTalent
    override val projectId get() = talentId
    override val title get() = "Talent"
}

data class StarRoute(override val slug: Slug): StreetlightRoute, SlugRoute {
    override val screen get() = StreetlightScreen.Star
    override val title get() = "Star"
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
    override val screen get() = StreetlightScreen.Docs
    override val title get() = "Documentation"

    override fun toSitePath() = toIdSitePath(docId)
}

data class TalkRoute(val id: Uuid, val type: SpaceType): StreetlightRoute {
    constructor(galaxyId: GalaxyId): this(galaxyId.value, SpaceType.Galaxy)

    override val screen get() = StreetlightScreen.Talk
    override val title get() = "Talk"

    override fun toSitePath() = toIdSitePath(id)
}

private fun AppRoute.toIdSitePath(id: String?) = id?.let { "$basePath/$id" } ?: basePath
private fun AppRoute.toIdSitePath(id: Slug?) = toIdSitePath(id?.string)
private fun AppRoute.toIdSitePath(id: TableId<Uuid>?) = toIdSitePath(id?.value.toString())
private fun AppRoute.toIdSitePath(id: Uuid?) = toIdSitePath(id?.toString())
