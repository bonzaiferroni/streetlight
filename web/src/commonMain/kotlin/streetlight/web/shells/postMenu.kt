package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.GalaxyKey

fun FlowContent.postMenu(galaxy: Galaxy) {
    buttonMenu("Create Post", modifiers = modify(Accent)) {
        card(ButtonMenu.CardMod) {
            btn("Post Event", EventScoutRoute(galaxy.path), modify(Accent))
            btn("Post Location", LocationScoutRoute(galaxy.path), modify(Accent))
        }
    }
}