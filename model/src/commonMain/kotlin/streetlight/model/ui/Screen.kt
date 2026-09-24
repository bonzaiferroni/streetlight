package streetlight.model.ui

import kampfire.api.ActionResult
import kampfire.api.SlugValue
import kampfire.api.TableId
import kampfire.model.Token
import kampfire.utils.pascalToKebabCase
import koala.html.AppRoute
import koala.html.AppScreen
import koala.html.IdParse
import koala.html.RouteParse
import koala.html.SlugOrNullParse
import koala.html.SlugParse
import koala.html.StaticParse
import koala.html.UsernameParse
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

/**
 * The screens of the app, each with the parse that turns its path into a route.
 *
 * A screen with a shell is rendered by the server on the initial load. A screen that retains within itself keeps
 * its view as its route changes.
 */
enum class Screen(
    override val routeParse: RouteParse,
    pathRoot: String? = null,
    override val hasShell: Boolean = false,
    override val retainWithinScreen: Boolean = false,
): AppScreen {
    Home(StaticParse { HomeRoute }, "", true),
    MediaUpdate(UuidParse { MediaUpdateRoute(it.toRecordId()) }),
    Sandbox(StaticParse { SandboxRoute }),
    Chat(StaticParse { ChatRoute }),
    SongProfile(UuidParse { SongProfileRoute(SongId(it)) }),
    Feedback(StaticParse { FrontDeskRoute }),
    SiteMonitor(StaticParse { SiteMonitorRoute }),
    Contribute(StaticParse { ContributeRoute }),

    // star
    TalentProfile(UuidParse { TalentProfileRoute(TalentId(it)) }),
    Star(UsernameParse { StarRoute(it) }, "s", true),
    StarDash(StaticParse { StarDashRoute }),
    ProfileConfig(StaticParse { ProfileConfigRoute }),
    StarConfig(StaticParse { StarConfigRoute }),
    Inbox(StaticParse { InboxRoute }),

    // token
    VerifyEmail(IdParse { VerifyEmailRoute(Token(it)) }),
    AccountNotOwned(IdParse { AccountNotOwnedRoute(Token(it)) }),
    PasswordReset(IdParse { PasswordResetRoute(Token(it)) }),
    AccountLockdown(IdParse { AccountLockdownRoute(Token(it)) }),
    ActionReport(IdParse { ActionReportRoute(ActionResult.of(it)) }),

    // location
    Location(SlugParse { LocationRoute(it) }, "l", true),
    LocationConfig(UuidParse { LocationConfigRoute(LocationId(it)) }),
    LocationScout(SlugOrNullParse { LocationScoutRoute(it) }),
    LocationUpdate(SlugParse { LocationUpdateRoute(it) }),

    // galaxy
    Galaxy(SlugParse { GalaxyRoute(it) }, "g", true),
    GalaxyFoundry(StaticParse { GalaxyFoundryRoute }),
    GalaxyConfig(SlugParse { GalaxyConfigRoute(it) } ),
    GalaxyList(StaticParse { GalaxyListRoute }, "galaxies"),

    // event
    Event(SlugParse { EventRoute(it) }, "e", true),
    UpdateEvent(SlugParse { EventUpdateRoute(it) }),

    EventScout(SlugOrNullParse { EventScoutRoute(it) }),
    MediaForge(SlugOrNullParse { MediaForgeRoute(it) }),
    SiteConfig(StaticParse { SiteConfigRoute }),
    AboutApp(StaticParse { AboutRoute }),
    Docs(IdParse { SiteDocRoute(it) }),
    Talk(UuidParse { TalkRoute(GalaxyId(it)) }),

    // earth
    Earth(parseEarthRoute, retainWithinScreen = true),

    // city
    City(SlugParse { CityRoute(it) }),
    CityConfig(SlugParse { CityConfigRoute(it) }),
    CityList(StaticParse { CityListRoute }, "cities", true),

    // media
    Media(SlugParse { MediaRoute(it) }, "m", true),

    Error(StaticParse { ErrorRoute });

    override val pathRoot = pathRoot ?: name.pascalToKebabCase()
    override val pathBase = "/${this.pathRoot}"
    override val screenId = name.pascalToKebabCase()
}

// interfaces
/** A route of the Streetlight app. */
sealed interface StreetlightRoute: AppRoute {
    // td: gather as build parameter
    override val origin get() = "http://localhost:8080"
}

/** A route whose path ends in a record id. */
interface RecordIdRoute: StreetlightRoute {
    val recordId: RecordId?

    override fun toRelativePath() = toIdSitePath(recordId)
}

/** A route whose path ends in a slug. */
sealed interface SlugRoute: StreetlightRoute {
    val slug: SlugValue?
    override fun toRelativePath() = toIdSitePath(slug)
}

/** A route whose path ends in a number. */
interface IntIdRoute: StreetlightRoute {
    val id: Int?
    override fun toRelativePath() = toIdSitePath(id)
}

/** A route whose path ends in a text id. */
interface StringIdRoute: StreetlightRoute {
    val id: String
    override fun toRelativePath() = toIdSitePath(id)
}

// singletons
object HomeRoute: StreetlightRoute {
    override val screen get() = Screen.Home
    override val title get() = "Home"
}

object ErrorRoute: StreetlightRoute {
    override val screen get() = Screen.Error
    override val title get() = "Alas"
}

object SandboxRoute: StreetlightRoute {
    override val screen get() = Screen.Sandbox
    override val title get() = "Sandbox"
}

object ChatRoute: StreetlightRoute {
    override val screen get() = Screen.Chat
    override val title get() = "Chat"
}

object SiteMonitorRoute: StreetlightRoute {
    override val screen get() = Screen.SiteMonitor
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

//data class EditTalentRoute(
//    val talentId: TalentId? = null
//): StreetlightRoute, RecordIdRoute {
//    override val screen get() = Screen.EditTalent
//    override val recordId get() = talentId
//    override val title get() = "Talent"
//}

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

    override fun toRelativePath() = toIdSitePath(docId)
}

data class TalkRoute(val id: Uuid, val type: SpaceType): StreetlightRoute {
    constructor(galaxyId: GalaxyId): this(galaxyId.value, SpaceType.Galaxy)

    override val screen get() = Screen.Talk
    override val title get() = "Talk"

    override fun toRelativePath() = toIdSitePath(id)
}

/** The route's base path followed by [id], or the base path alone when it is `null`. */
fun AppRoute.toIdSitePath(id: String?) = id?.let { "$basePath/$id" } ?: basePath
fun AppRoute.toIdSitePath(id: SlugValue?) = toIdSitePath(id?.value)
fun AppRoute.toIdSitePath(id: TableId<Uuid>?) = toIdSitePath(id?.value.toString())
fun AppRoute.toIdSitePath(id: Uuid?) = toIdSitePath(id?.toString())
fun AppRoute.toIdSitePath(id: Int?) = toIdSitePath(id?.toString())
