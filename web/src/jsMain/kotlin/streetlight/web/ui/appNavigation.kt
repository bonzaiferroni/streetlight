package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.SlideUp
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.dom.viewContextOf
import kotlinx.browser.window
import streetlight.web.StreetlightScreen
import streetlight.web.model.Streetlight

fun RenderContext.appNavigation(app: Streetlight) {
    flowBlock(
        flow = app.portal.screenFlow,
        modifiers = modify(Magic, Blur, SlideUp),
        renderCacheCount = 0, // allows unlimited caching
        onTransition = { window.scrollTo(0.0, app.portal.stateNow.initialScroll) },
    ) { screen ->
        viewContextOf(app) {
            when (screen) {
                StreetlightScreen.Home -> viewHome()
                StreetlightScreen.EventProfile -> viewEventProfileRoute()
                StreetlightScreen.StarDash -> viewStarDash(app)
                StreetlightScreen.EditEvent -> viewEventEditorRoute()
                StreetlightScreen.Sandbox -> viewSandbox(app)
                StreetlightScreen.Earth -> viewEarthMapRoute()
                StreetlightScreen.EditStory -> viewPostEditor(app)
                StreetlightScreen.Chat -> viewChatRoom(app)
                StreetlightScreen.SongProfile -> viewSongProfile(app)
                StreetlightScreen.EditTalent -> editTalentForm(app)
                StreetlightScreen.Location -> viewLocationProfile(app)
                StreetlightScreen.EditLocation -> viewEditLocationRoute(app)
                StreetlightScreen.LocationAdmin -> viewLocationAdmin(app)
                StreetlightScreen.CreateGalaxy -> viewGalaxyFoundry()
                StreetlightScreen.GalaxyList -> viewGalaxyList(app)
                StreetlightScreen.Galaxy -> viewGalaxyRoute()
                StreetlightScreen.Star -> viewStarProfileRoute()
                StreetlightScreen.EventScout -> viewEventScoutRoute()
                StreetlightScreen.LocationScout -> viewLocationScoutRoute()
                StreetlightScreen.PostContent -> viewContentPosterRoute()
                StreetlightScreen.EditStar -> viewStarEditor()
                StreetlightScreen.SiteConfig -> viewSiteConfig()
                StreetlightScreen.AboutApp -> viewAboutApp()
                StreetlightScreen.PrivacyPolicy -> viewPrivacyPolicy()
                StreetlightScreen.SiteDoc -> viewSiteDocRoute()
                StreetlightScreen.Talk -> viewTalkRoute()
                else -> textBlock("Coming soon: $screen")
            }
        }
    }
}