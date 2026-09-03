package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.MediaForgeRoute
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.PopoverId

fun FlowContent.createPostMenu(galaxy: Galaxy? = null) {
    button("Create Post", modify(Accent), "☰") {
        setPopoverTarget(PopoverId.CreatePost)
        galaxy?.let {
            setAttribute(Attribute.Slug.to(galaxy.slug))
        }
    }
}