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
import streetlight.web.model.AppSection
import streetlight.web.model.toAppSection
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
            when ((screen as Screen).toAppSection()) {
                AppSection.Core -> viewCorePortal()
                AppSection.Studio -> lazyRoute(lazyStudio)
            }
        }
    }
}

/** The view of the current screen in the Core section. */
private fun RouteScope.viewCorePortal() {
    when (val screen = state.route.screen) {
        Screen.Home -> viewHomeRoute()
        Screen.Earth -> viewEarthRoute()
        Screen.SongProfile -> viewSongProfileRoute()

        // star
        Screen.Star -> viewStarRoute()
        Screen.StarFeed -> viewStarFeedRoute()

        // location
        Screen.Location -> viewLocation()

        // event
        Screen.Event -> viewEventRoute()

        // galaxy
        Screen.Galaxy -> viewGalaxyRoute()
        Screen.GalaxyFeed -> viewGalaxyFeedRoute()

        // city
        Screen.CityFeed -> viewCityFeedRoute()
        Screen.City -> viewCityRoute()

        // media
        Screen.Media -> viewMediaRoute()

        // offering
        Screen.Offering -> viewOfferingRoute()

        // skill
        Screen.SkillProfile -> viewSkillRoute()

        // other
        Screen.AboutApp -> viewAboutApp()
        Screen.Connect -> viewConnectRoute()
        Screen.Contribute -> viewContributeRoute()
        else -> textBlock("Coming soon: $screen")
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
