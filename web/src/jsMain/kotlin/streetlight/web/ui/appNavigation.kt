package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.modify
import koala.dom.ScopedDOM
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.browser.window
import streetlight.web.StreetlightScreen

fun ScopedDOM.appNavigation() {
    val portal = app.get<Portal>()

    flowBlock(
        flow = portal.screenFlow,
        modifiers = modify(Magic, Blur),
        renderCacheCount = 0, // allows unlimited caching
        onTransition = { window.scrollTo(0.0, portal.stateNow.initialScrollY) },
    ) { screen ->
        when (screen) {
            StreetlightScreen.Home -> viewHomeRoute()
            StreetlightScreen.Event -> viewEventProfileRoute()
            StreetlightScreen.Account -> viewStarDash()
            StreetlightScreen.UpdateEvent -> viewEventEditorRoute()
            StreetlightScreen.Sandbox -> viewSandbox()
            StreetlightScreen.Earth -> viewEarthMapRoute()
            StreetlightScreen.Post -> viewPostRoute()
            StreetlightScreen.UpdatePost -> viewEditPostRoute()
            StreetlightScreen.Chat -> viewChatRoom()
            StreetlightScreen.SongProfile -> viewSongProfile()
            StreetlightScreen.EditTalent -> editTalentForm()

            // location
            StreetlightScreen.LocationAdmin -> viewLocationAdmin()
            StreetlightScreen.Location -> viewLocationProfile()
            StreetlightScreen.LocationUpdate -> viewEditLocationRoute()
            StreetlightScreen.LocationScout -> viewLocationScoutRoute()

            // event
            StreetlightScreen.EventScout -> viewEventScoutRoute()

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