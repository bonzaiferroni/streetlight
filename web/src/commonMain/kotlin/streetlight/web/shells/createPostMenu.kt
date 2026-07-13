package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.MediaForgeRoute

fun FlowContent.createPostMenu(galaxy: Galaxy) {
    buttonPopover("Create Post", modify(Accent)) {
        card(ButtonPopover.CardMod) {
            btn("Post Event", EventScoutRoute(galaxy.slug))
            btn("Post Location", LocationScoutRoute(galaxy.slug))
            btn("Post Media", MediaForgeRoute(galaxy.slug))
        }
    }
}