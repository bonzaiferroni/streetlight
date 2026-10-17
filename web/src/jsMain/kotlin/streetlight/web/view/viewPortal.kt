package streetlight.web.view

import koala.modifier.Blur
import koala.modifier.FocusTarget
import koala.modifier.Magic
import koala.modifier.modify
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.closeOpenPopovers
import koala.dom.layout.flowBlock
import koala.dom.querySelector
import koala.dom.component.textBlock
import koala.model.Portal
import streetlight.model.ui.Screen
import koala.model.RouteInflator
import web.dom.document
import web.html.HTMLElement
import streetlight.web.model.Sandbox
import streetlight.web.utility.element
import streetlight.web.model.portal

fun ViewScope.viewPortal() {
    val portal = app.get<Portal>()
    val inflator = app.get<RouteInflator>()
    var element: HTMLElement? = null

    element = flowBlock(
        tap = portal.screenState,
        mod = modify(Magic),
        name = ::viewPortal.name,
        // cacheElements = true,
        onTransition = {
            document.closeOpenPopovers()
            // td: set title, maybe not here
            // target element, typically a heading, for accessibility functionality
            element?.querySelector(FocusTarget)?.focus()
        },
    ) { screen ->
        val routeScope = RouteScope(this, inflator, portal.stateNow)
        RouteView.setActiveScope(routeScope)
        with (routeScope) {
            when (screen) {
                Screen.Home -> viewHomeRoute()
                Screen.Sandbox -> viewSandbox()
                Screen.Earth -> viewEarthRoute()
                Screen.MediaUpdate -> viewMediaConfigRoute()
                Screen.Chat -> viewChatRoom()
                Screen.SongProfile -> viewSongProfile()
                Screen.Feedback -> viewFrontDeskRoute()
                Screen.SiteMonitor -> viewSiteMonitor()

                // star
                Screen.Star -> viewStarRoute()
                Screen.StarDash -> viewStarDashRoute()
                Screen.ProfileConfig -> viewProfileConfigRoute()
                Screen.StarConfig -> viewStarConfigRoute()
                Screen.Inbox -> viewInboxRoute()

                // location
                Screen.LocationConfig -> viewLocationConfigRoute()
                Screen.Location -> viewLocation()
                Screen.LocationUpdate -> viewLocationUpdaterRoute()
                Screen.LocationScout -> viewLocationScoutRoute()

                // event
                Screen.Event -> viewEventRoute()
                Screen.EventScout -> viewEventScoutRoute()
                Screen.UpdateEvent -> viewEventUpdaterRoute()

                // galaxy
                Screen.Galaxy -> viewGalaxyRoute()
                Screen.GalaxyFoundry -> viewGalaxyFoundry()
                Screen.GalaxyConfig -> viewGalaxyConfigRoute()
                Screen.GalaxyFeed -> viewGalaxyFeedRoute()

                // city
                Screen.CityFeed -> viewCityFeedRoute()
                Screen.City -> viewCityRoute()
                Screen.CityConfig -> viewCityConfigRoute()

                // media
                Screen.MediaForge -> viewMediaForgeRoute()
                Screen.Media -> viewMediaRoute()

                // offering
                Screen.Offering -> viewOfferingRoute()

                // other
                Screen.SiteConfig -> viewSiteConfig()
                Screen.AboutApp -> viewAboutApp()
                Screen.Connect -> viewConnectRoute()
                Screen.Onboard -> viewOnboardRoute()
                Screen.Docs -> viewSiteDocRoute()
                Screen.Talk -> viewTalkRoute()
                else -> textBlock("Coming soon: $screen")
            }
        }
    }
}

// td: consider making this a service
/** The route scope currently shown, for handlers outside the view tree. */
object RouteView {
    private var _activeScope: RouteScope? = null
    val activeScope: RouteScope get() = _activeScope ?: error("active route scope not found")

    fun setActiveScope(scope: RouteScope) {
        _activeScope = scope
    }
}
