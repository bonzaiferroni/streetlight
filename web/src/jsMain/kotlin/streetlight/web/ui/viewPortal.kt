package streetlight.web.ui

import koala.css.Blur
import koala.css.FocusTarget
import koala.css.Magic
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.closeOpenPopovers
import koala.dom.flowBlock
import koala.dom.querySelector
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import streetlight.model.ui.Screen
import streetlight.web.model.RouteInflator
import streetlight.web.model.RouteScope

fun ViewScope.viewPortal() {
    val portal = app.get<Portal>()
    val inflator = app.get<RouteInflator>()
    var element: HTMLElement? = null

    element = flowBlock(
        flow = portal.screenFlow,
        modifiers = modify(Magic, Blur),
        name = ::viewPortal.name,
        // cacheElements = true,
        onTransition = {
            if (!portal.stateNow.isInitialRoute) {
                window.setTimeout({
                    window.scrollTo(0.0, portal.stateNow.initialScrollY)
                }, 100)
            }
            document.closeOpenPopovers()

            // td: set title, maybe not here
            // target element, typically a heading, for accessibility functionality
            element?.querySelector(FocusTarget)?.focus()
        },
    ) { screen ->
        val routeScope = RouteScope(this, portal.stateNow.route, inflator)
        with (routeScope) {
            when (screen) {
                Screen.Home -> viewHomeRoute()
                Screen.Sandbox -> viewSandbox()
                Screen.Earth -> viewEarthRoute()
                Screen.MediaUpdate -> viewMediumUpdaterRoute()
                Screen.Chat -> viewChatRoom()
                Screen.SongProfile -> viewSongProfile()
                Screen.EditTalent -> editTalentForm()
                Screen.Feedback -> viewFrontDeskRoute()
                Screen.SiteMonitor -> viewSiteMonitor()

                // star
                Screen.Star -> viewStarRoute()
                Screen.StarDash -> viewStarDashRoute()
                Screen.StarConfig -> viewStarConfigRoute()

                // location
                Screen.LocationAdmin -> viewLocationAdmin()
                Screen.Location -> viewLocation()
                Screen.LocationUpdate -> viewUpdateLocationRoute()
                Screen.LocationScout -> viewLocationScoutRoute()

                // event
                Screen.Event -> viewEventRoute()
                Screen.EventScout -> viewEventScoutRoute()
                Screen.UpdateEvent -> viewEventUpdaterRoute()

                // galaxy
                Screen.Galaxy -> viewGalaxyRoute(inflator)
                Screen.GalaxyFoundry -> viewGalaxyFoundry()
                Screen.GalaxyConfig -> viewGalaxyConfigRoute()
                Screen.GalaxyList -> viewGalaxyList()

                Screen.MediaForge -> viewContentPosterRoute()
                Screen.SiteConfig -> viewSiteConfig()
                Screen.AboutApp -> viewAboutApp()
                Screen.Docs -> viewSiteDocRoute()
                Screen.Talk -> viewTalkRoute()
                else -> textBlock("Coming soon: $screen")
            }
        }
    }
}