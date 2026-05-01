package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.PostContentRoute

fun FlowContent.postMenu(galaxy: Galaxy) {
    buttonPopover("Create Post", modify(Accent)) {
        card(ButtonPopover.CardMod) {
            btn("Post Event", EventScoutRoute(galaxy.slug))
            btn("Post Location", LocationScoutRoute(galaxy.slug))
            btn("Post Content", PostContentRoute(galaxy.slug))
        }
    }
}