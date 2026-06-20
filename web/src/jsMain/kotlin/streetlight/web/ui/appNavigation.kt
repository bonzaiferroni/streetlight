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
import streetlight.web.StreetlightScreen

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
            StreetlightScreen.Home -> viewHomeRoute()
            StreetlightScreen.Event -> viewEventProfileRoute()
            StreetlightScreen.Account -> viewStarDashRoute()
            StreetlightScreen.Sandbox -> viewSandbox()
            StreetlightScreen.Earth -> viewEarthMapRoute()
            StreetlightScreen.Post -> viewPostRoute()
            StreetlightScreen.UpdatePost -> viewEditPostRoute()
            StreetlightScreen.Chat -> viewChatRoom()
            StreetlightScreen.SongProfile -> viewSongProfile()
            StreetlightScreen.EditTalent -> editTalentForm()

            // location
            StreetlightScreen.LocationAdmin -> viewLocationAdmin()
            StreetlightScreen.Location -> viewLocation()
            StreetlightScreen.LocationUpdate -> viewUpdateLocationRoute()
            StreetlightScreen.LocationScout -> viewLocationScoutRoute()

            // event
            StreetlightScreen.EventScout -> viewEventScoutRoute()
            StreetlightScreen.UpdateEvent -> viewEventEditorRoute()

            // galaxy
            StreetlightScreen.Galaxy -> viewGalaxyRoute()
            StreetlightScreen.GalaxyFoundry -> viewGalaxyFoundry()
            StreetlightScreen.GalaxyUpdate -> viewGalaxyConfigRoute()
            StreetlightScreen.Galaxies -> viewGalaxyList()

            StreetlightScreen.Star -> viewStarProfileRoute()
            StreetlightScreen.CreatePost -> viewContentPosterRoute()
            StreetlightScreen.EditStar -> viewStarEditor()
            StreetlightScreen.SiteConfig -> viewSiteConfig()
            StreetlightScreen.AboutApp -> viewAboutApp()
            StreetlightScreen.Docs -> viewSiteDocRoute()
            StreetlightScreen.Talk -> viewTalkRoute()
            else -> textBlock("Coming soon: $screen")
        }
    }
}