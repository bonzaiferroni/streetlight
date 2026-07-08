package streetlight.web

import kampfire.api.SlugValue
import kampfire.api.TableId
import kampfire.utils.pascalToKebabCase
import koala.html.AppRoute
import koala.html.AppScreen
import koala.html.IdParse
import koala.html.RouteParse
import koala.html.SlugParse
import koala.html.StaticParse
import koala.html.UuidParse
import koala.model.DocId
import streetlight.model.data.GalaxyId
import streetlight.model.data.LocationId
import streetlight.model.data.RecordId
import streetlight.model.data.SongId
import streetlight.model.data.TalentId
import streetlight.model.data.SpaceType
import streetlight.model.data.toRecordId
import kotlin.uuid.Uuid

enum class Screen(
    override val routeParse: RouteParse,
    pathRoot: String? = null,
): AppScreen {
    Home(StaticParse { HomeRoute }, ""),
    Account(StaticParse { StarDashRoute }),
    MediaUpdate(UuidParse { MediaUpdateRoute(it.toRecordId()) }),
    Sandbox(StaticParse { SandboxRoute }),
    Chat(StaticParse { ChatRoute }),
    SongProfile(UuidParse { SongProfileRoute(SongId(it)) }),
    TalentProfile(UuidParse { TalentProfileRoute(TalentId(it)) }),
    EditTalent(UuidParse { EditTalentRoute(TalentId(it)) }),
    Feedback(StaticParse { FrontDeskRoute }),
    Status(StaticParse { StatusRoute }),
    Contribute(StaticParse { ContributeRoute }),

    // location
    Location(SlugParse { LocationRoute(it) }, "l"),
    LocationAdmin(UuidParse { LocationAdminRoute(LocationId(it)) }),
    LocationScout(SlugParse { LocationScoutRoute(it) }),
    LocationUpdate(SlugParse { LocationUpdateRoute(it) }),

    // galaxy
    Galaxy(SlugParse { GalaxyRoute(it) }, "g"),
    GalaxyFoundry(StaticParse { GalaxyFoundryRoute }),
    GalaxyUpdate(SlugParse { GalaxyConfigRoute(it) } ),
    GalaxyList(StaticParse { GalaxyListRoute }, "galaxies"),

    // event
    Event(SlugParse { EventRoute(it) }, "e"),
    UpdateEvent(SlugParse { EventUpdateRoute(it) }),

    Star(SlugParse { StarRoute(it) }, "s"),
    EventScout(SlugParse { EventScoutRoute(it) }),
    MediaForge(SlugParse { MediaForgeRoute(it) }),
    EditStar(StaticParse { EditStarRoute }),
    SiteConfig(StaticParse { SiteConfigRoute }),
    AboutApp(StaticParse { AboutRoute }),
    Docs(IdParse { SiteDocRoute(it) }),
    Talk(UuidParse { TalkRoute(GalaxyId(it)) }),

    // earth
    Earth(parseEarthRoute),

    // city
    City(SlugParse { CityRoute(it) }),
    CityList(StaticParse { CityListRoute }, "cities"),

    // media
    Media(SlugParse { MediaRoute(it) }, "m");

    override val pathRoot = pathRoot ?: name.pascalToKebabCase()
    override val screenId = name.pascalToKebabCase()
}

// interfaces
sealed interface StreetlightRoute: AppRoute

interface RecordIdRoute: StreetlightRoute {
    val recordId: RecordId?

    override fun toSitePath() = toIdSitePath(recordId)
}

sealed interface SlugRoute: StreetlightRoute {
    val slug: SlugValue?
    override fun toSitePath() = toIdSitePath(slug)
}

interface IntIdRoute: StreetlightRoute {
    val id: Int?
    override fun toSitePath() = toIdSitePath(id)
}

// singletons
object HomeRoute: StreetlightRoute {
    override val screen get() = Screen.Home
    override val title get() = "Home"
}

object StarDashRoute: StreetlightRoute {
    override val screen get() = Screen.Account
    override val title get() = "You"
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = Screen.Sandbox
    override val title get() = "Sandbox"
}

object ChatRoute: StreetlightRoute {
    override val screen get() = Screen.Chat
    override val title get() = "Chat"
}

object StatusRoute: StreetlightRoute {
    override val screen get() = Screen.Status
    override val title get() = "Status"
}

object ContributeRoute: StreetlightRoute {
    override val screen get() = Screen.Contribute
    override val title get() = "Contribute"
}

// data class
data class SongProfileRoute(
    val songId: SongId
): StreetlightRoute, RecordIdRoute {
    override val screen get() = Screen.SongProfile
    override val recordId get() = songId
    override val title get() = "Song"
}

data class TalentProfileRoute(
    val talentId: TalentId
): StreetlightRoute, RecordIdRoute {
    override val screen get() = Screen.TalentProfile
    override val recordId get() = talentId
    override val title get() = "Talent"
}

data class EditTalentRoute(
    val talentId: TalentId? = null
): StreetlightRoute, RecordIdRoute {
    override val screen get() = Screen.EditTalent
    override val recordId get() = talentId
    override val title get() = "Talent"
}

data class StarRoute(override val slug: SlugValue): StreetlightRoute, SlugRoute {
    override val screen get() = Screen.Star
    override val title get() = "Star"
}

object EditStarRoute: StreetlightRoute {
    override val screen get() = Screen.EditStar
    override val title get() = "Edit Profile"
}

object SiteConfigRoute: StreetlightRoute {
    override val screen get() = Screen.SiteConfig
    override val title get() = "Config"
}

object AboutRoute: StreetlightRoute {
    override val screen get() = Screen.AboutApp
    override val title get() = "About"
}

data class SiteDocRoute(val docId: DocId): StreetlightRoute {
    override val screen get() = Screen.Docs
    override val title get() = "Documentation"

    override fun toSitePath() = toIdSitePath(docId)
}

data class TalkRoute(val id: Uuid, val type: SpaceType): StreetlightRoute {
    constructor(galaxyId: GalaxyId): this(galaxyId.value, SpaceType.Galaxy)

    override val screen get() = Screen.Talk
    override val title get() = "Talk"

    override fun toSitePath() = toIdSitePath(id)
}

fun AppRoute.toIdSitePath(id: String?) = id?.let { "$basePath/$id" } ?: basePath
fun AppRoute.toIdSitePath(id: SlugValue?) = toIdSitePath(id?.value)
fun AppRoute.toIdSitePath(id: TableId<Uuid>?) = toIdSitePath(id?.value.toString())
fun AppRoute.toIdSitePath(id: Uuid?) = toIdSitePath(id?.toString())
fun AppRoute.toIdSitePath(id: Int?) = toIdSitePath(id?.toString())
