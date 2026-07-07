package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.modify
import koala.dom.AppScope
import koala.dom.closeOpenPopovers
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.browser.document
import kotlinx.browser.window
import streetlight.web.Screen

fun AppScope.appNavigation() {
    val portal = app.get<Portal>()

    flowBlock(
        flow = portal.screenFlow,
        modifiers = modify(Magic, Blur),
        cacheElements = true,
        onTransition = {
            if (!portal.stateNow.isInitialRoute) {
                window.scrollTo(0.0, portal.stateNow.initialScrollY)
            }
            document.closeOpenPopovers()
        },
    ) { screen ->
        when (screen) {
            Screen.Home -> viewHomeRoute()
            Screen.Event -> viewEventProfileRoute()
            Screen.Account -> viewStarDashRoute()
            Screen.Sandbox -> viewSandbox()
            Screen.Earth -> viewEarthRoute()
            Screen.MediaUpdate -> viewMediumUpdaterRoute()
            Screen.Chat -> viewChatRoom()
            Screen.SongProfile -> viewSongProfile()
            Screen.EditTalent -> editTalentForm()
            Screen.Feedback -> viewFrontDeskRoute()

            // location
            Screen.LocationAdmin -> viewLocationAdmin()
            Screen.Location -> viewLocation()
            Screen.LocationUpdate -> viewUpdateLocationRoute()
            Screen.LocationScout -> viewLocationScoutRoute()

            // event
            Screen.EventScout -> viewEventScoutRoute()
            Screen.UpdateEvent -> viewEventUpdaterRoute()

            // galaxy
            Screen.Galaxy -> viewGalaxyRoute()
            Screen.GalaxyFoundry -> viewGalaxyFoundry()
            Screen.GalaxyUpdate -> viewGalaxyConfigRoute()
            Screen.GalaxyList -> viewGalaxyList()

            Screen.Star -> viewStarProfileRoute()
            Screen.MediaForge -> viewContentPosterRoute()
            Screen.EditStar -> viewStarEditor()
            Screen.SiteConfig -> viewSiteConfig()
            Screen.AboutApp -> viewAboutApp()
            Screen.Docs -> viewSiteDocRoute()
            Screen.Talk -> viewTalkRoute()
            else -> textBlock("Coming soon: $screen")
        }
    }
}