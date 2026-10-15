package streetlight.model.ui

import io.ktor.http.Parameters
import kampfire.api.Username
import kampfire.api.intParamOf
import kampfire.api.read
import koala.model.buildRelativePath

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

data class StarConfigRoute(val tabIndex: Int? = null): StreetlightRoute {
    companion object {
        val tabIndexParam = intParamOf("tab-index")

        fun of(parameters: Parameters) = StarConfigRoute(parameters.read(tabIndexParam))
    }

    override val screen get() = Screen.StarConfig
    override val title get() = "Account"
    override val screenKey get() = "star-config"
    override fun toRelativePath() = buildRelativePath {
        writeParam(tabIndexParam, tabIndex)
    }
}

object InboxRoute: StreetlightRoute {
    override val screen get() = Screen.Inbox
    override val title get() = "Inbox"
}