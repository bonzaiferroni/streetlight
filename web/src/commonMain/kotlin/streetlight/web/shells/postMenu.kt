package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.LocationScoutRoute

fun FlowContent.postMenu(galaxy: Galaxy) {
    buttonMenu("Create Post", modify(Accent)) {
        card(ButtonMenu.CardMod) {
            btn("Post Event", EventScoutRoute(galaxy.slug), modify(Accent))
            btn("Post Location", LocationScoutRoute(galaxy.slug), modify(Accent))
        }
    }
}