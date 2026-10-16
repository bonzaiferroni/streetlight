package streetlight.model.ui

import io.ktor.http.Parameters
import kampfire.api.Username
import kampfire.api.ordinalParamOf
import kampfire.api.read
import kampfire.api.tableIdParamOf
import kampfire.model.Labeled
import koala.SvgFile
import koala.SvgPack
import koala.model.buildRelativePath
import streetlight.model.data.OfferingId
import kotlin.uuid.Uuid

data class StarRoute(val username: Username): StreetlightRoute, SlugRoute {
    override val screen get() = Screen.Star
    override val title get() = "Profile"
    override val slug get() = username
}

object StarDashRoute: StreetlightRoute {
    override val screen get() = Screen.StarDash
    override val title get() = "Messages"
}

object ProfileConfigRoute: StreetlightRoute {
    override val screen get() = Screen.ProfileConfig
    override val title get() = "Config"
}

data class StarConfigRoute(val tab: StarConfigTab? = null): StreetlightRoute {
    companion object {
        val tabParam = ordinalParamOf<StarConfigTab>("tab")

        fun of(parameters: Parameters) = StarConfigRoute(parameters.read(tabParam))
    }

    override val screen get() = Screen.StarConfig
    override val title get() = "Account"
    override val screenKey get() = "star-config"
    override fun toRelativePath() = buildRelativePath {
        writeParam(tabParam, tab)
    }
}

object InboxRoute: StreetlightRoute {
    override val screen get() = Screen.Inbox
    override val title get() = "Inbox"
}

enum class StarConfigTab(val svg: SvgPack, label: String? = null): Labeled {
    Account(SvgFile.User),
    ConnectApps(SvgFile.Affiliate, "connect apps"),
    ECommerce(SvgFile.BuildingStore, "e-commerce");

    override val label = label ?: name
}