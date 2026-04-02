package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.GalaxyKey

fun DIV.postMenu(galaxy: Galaxy) {
    popover(PostMenuKey.Id, PostMenuKey.Anchor, modify(Magic, SlideUp)) {
        card(modify(BlurBackdrop, BorderRadius4, PrimaryCardBg, Margin1)) {
            btn("Post Event", EventScoutRoute(galaxy.path))
            btn("Post Location", LocationScoutRoute(galaxy.path))
        }
    }
    button("☰ Create Post", modify(Accent)) {
        setAnchor(PostMenuKey.Anchor)
        setAttribute(Attribute.PopoverTarget, PostMenuKey.Id.identifier)
    }
}

object PostMenuKey {
    val Id = Id("create-post-menu")
    val Anchor = Anchor("create-post-menu")
}