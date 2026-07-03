package streetlight.web

import kampfire.api.Slug
import kampfire.api.SlugValue
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
import streetlight.model.data.RecordId
import streetlight.model.data.SongId
import streetlight.model.data.TalentId
import streetlight.model.data.SpaceType
import streetlight.model.data.toRecordId
import kotlin.uuid.Uuid

enum class StreetlightScreen(
    override val routeParse: RouteParse,
    pathRoot: String? = null,
): AppScreen {
    Home(StaticParse { HomeRoute }, ""),
    Account(StaticParse { StarDashRoute }),
    MediaUpdate(UuidParse { MediaUpdateRoute(it.toRecordId()) }),
    Sandbox(StaticParse { SandboxRoute }),
    Earth(SlugOrNullParse { EarthRoute(it) }),
    Chat(StaticParse { ChatRoute }),
    SongProfile(UuidParse { SongProfileRoute(SongId(it)) }),
    TalentProfile(UuidParse { TalentProfileRoute(TalentId(it)) }),
    EditTalent(UuidParse { EditTalentRoute(TalentId(it)) }),

    // location
    Location(SlugParse { LocationRoute(it) }, "l"),
    LocationAdmin(UuidParse { LocationAdminRoute(LocationId(it)) }),
    LocationScout(SlugParse { LocationScoutRoute(it) }),
    LocationUpdate(SlugParse { LocationUpdateRoute(it) }),

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
    MediaForge(SlugParse { MediaForgeRoute(it) }),
    EditStar(StaticParse { EditStarRoute }),
    SiteConfig(StaticParse { SiteConfigRoute }),
    AboutApp(StaticParse { AboutRoute }),
    Docs(IdParse { SiteDocRoute(it) }),
    Talk(UuidParse { TalkRoute(GalaxyId(it)) }),

    // media
    Media(SlugParse { MediaRoute(it) }, "m");

    override val pathRoot = pathRoot ?: name.pascalToKebabCase()
}

sealed interface StreetlightRoute: AppRoute

interface RecordIdRoute: StreetlightRoute {
    val recordId: RecordId?

    override fun toSitePath() = toIdSitePath(recordId)
}

sealed interface SlugRoute: StreetlightRoute {
    val slug: SlugValue?
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

data class EarthRoute(override val slug: Slug?): SlugRoute {
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
): StreetlightRoute, RecordIdRoute {
    override val screen get() = StreetlightScreen.SongProfile
    override val recordId get() = songId
    override val title get() = "Song"
}

data class TalentProfileRoute(
    val talentId: TalentId
): StreetlightRoute, RecordIdRoute {
    override val screen get() = StreetlightScreen.TalentProfile
    override val recordId get() = talentId
    override val title get() = "Talent"
}

data class EditTalentRoute(
    val talentId: TalentId? = null
): StreetlightRoute, RecordIdRoute {
    override val screen get() = StreetlightScreen.EditTalent
    override val recordId get() = talentId
    override val title get() = "Talent"
}

data class StarRoute(override val slug: SlugValue): StreetlightRoute, SlugRoute {
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
private fun AppRoute.toIdSitePath(id: SlugValue?) = toIdSitePath(id?.value)
private fun AppRoute.toIdSitePath(id: TableId<Uuid>?) = toIdSitePath(id?.value.toString())
private fun AppRoute.toIdSitePath(id: Uuid?) = toIdSitePath(id?.toString())
