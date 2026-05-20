package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.browser.window
import streetlight.web.StreetlightScreen

fun RenderContext.appNavigation() {
    val portal = app.get<Portal>()

    flowBlock(
        flow = portal.screenFlow,
        modifiers = modify(Magic, Blur),
        renderCacheCount = 0, // allows unlimited caching
        onTransition = { window.scrollTo(0.0, portal.stateNow.initialScrollY) },
    ) { screen ->
        when (screen) {
            StreetlightScreen.Home -> viewHomeRoute()
            StreetlightScreen.EventProfile -> viewEventProfileRoute()
            StreetlightScreen.StarDash -> viewStarDash()
            StreetlightScreen.EditEvent -> viewEventEditorRoute()
            StreetlightScreen.Sandbox -> viewSandbox()
            StreetlightScreen.Earth -> viewEarthMapRoute()
            StreetlightScreen.Post -> viewStarPostRoute()
            StreetlightScreen.EditPost -> viewEditPostRoute()
            StreetlightScreen.Chat -> viewChatRoom()
            StreetlightScreen.SongProfile -> viewSongProfile()
            StreetlightScreen.EditTalent -> editTalentForm()

            // location
            StreetlightScreen.LocationAdmin -> viewLocationAdmin()
            StreetlightScreen.Location -> viewLocationProfile()
            StreetlightScreen.EditLocation -> viewEditLocationRoute()
            StreetlightScreen.LocationScout -> viewLocationScoutRoute()

            // galaxy
            StreetlightScreen.Galaxy -> viewGalaxyRoute()
            StreetlightScreen.GalaxyFoundry -> viewGalaxyFoundry()
            StreetlightScreen.GalaxyConfig -> viewGalaxyConfigRoute()
            StreetlightScreen.GalaxyList -> viewGalaxyList()

            StreetlightScreen.Star -> viewStarProfileRoute()
            // StreetlightScreen.EventScout -> viewEventScoutProtoRoute()
            StreetlightScreen.PostContent -> viewContentPosterRoute()
            StreetlightScreen.EditStar -> viewStarEditor()
            StreetlightScreen.SiteConfig -> viewSiteConfig()
            StreetlightScreen.AboutApp -> viewAboutApp()
            StreetlightScreen.SiteDoc -> viewSiteDocRoute()
            StreetlightScreen.Talk -> viewTalkRoute()
            else -> textBlock("Coming soon: $screen")
        }
    }
}