package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.component.textBlock
import streetlight.model.ui.Screen

/** The view of the current screen in the Studio section. */
fun RouteScope.viewStudioPortal() {
    when (val screen = state.route.screen) {
        Screen.MediaUpdate -> viewMediaConfigRoute()
        Screen.ProfileConfig -> viewProfileConfigRoute()
        Screen.StarConfig -> viewStarConfigRoute()
        Screen.Onboard -> viewOnboardRoute()
        Screen.StarDash -> viewStarDashRoute()
        Screen.Inbox -> viewInboxRoute()
        Screen.Chat -> viewChatRoom()
        Screen.Feedback -> viewFrontDeskRoute()

        // location
        Screen.LocationConfig -> viewLocationConfigRoute()
        Screen.LocationUpdate -> viewLocationUpdaterRoute()
        Screen.LocationScout -> viewLocationScoutRoute()

        // event
        Screen.EventScout -> viewEventScoutRoute()
        Screen.UpdateEvent -> viewEventUpdaterRoute()

        // galaxy
        Screen.GalaxyFoundry -> viewGalaxyFoundry()
        Screen.GalaxyConfig -> viewGalaxyConfigRoute()

        // city
        Screen.CityConfig -> viewCityConfigRoute()

        // media
        Screen.MediaForge -> viewMediaForgeRoute()

        // other
        Screen.SiteConfig -> viewSiteConfig()
        Screen.SiteMonitor -> viewSiteMonitor()
        Screen.Sandbox -> viewSandbox()
        Screen.Docs -> viewSiteDocRoute()
        Screen.Talk -> viewTalkRoute()
        else -> textBlock("Coming soon: $screen")
    }
}
