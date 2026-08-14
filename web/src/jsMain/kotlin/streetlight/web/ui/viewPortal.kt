package streetlight.web.ui

import koala.css.Blur
import koala.css.FocusTarget
import koala.css.Magic
import koala.css.Scale
import koala.css.SlideUp
import koala.css.modify
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.closeOpenPopovers
import koala.dom.flowBlock
import koala.dom.querySelector
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import streetlight.model.ui.Screen
import koala.model.RouteInflator

fun ViewScope.viewPortal() {
    val portal = app.get<Portal>()
    val inflator = app.get<RouteInflator>()
    var element: HTMLElement? = null

    element = flowBlock(
        tap = portal.screenField,
        modifiers = modify(Magic, Blur),
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
                Screen.UpdateProfile -> viewUpdateProfileRoute()
                Screen.UpdateAccount -> viewUpdateAccountRoute()

                // location
                Screen.LocationAdmin -> viewLocationConfigRoute()
                Screen.Location -> viewLocation()
                Screen.LocationUpdate -> viewUpdateLocationRoute()
                Screen.LocationScout -> viewLocationScoutRoute()

                // event
                Screen.Event -> viewEventRoute()
                Screen.EventScout -> viewEventScoutRoute()
                Screen.UpdateEvent -> viewEventUpdaterRoute()

                // galaxy
                Screen.Galaxy -> viewGalaxyRoute()
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