package streetlight.web.ui

import koala.dom.*
import koala.html.Attribute
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.MediaForgeRoute

fun ViewScope.wireCreatePost() {
    popoverMenu(
        popoverId = PopoverId.CreatePost,
        transform = {
            it.getAttribute(Attribute.Slug)
        }
    ) { slug ->
        column(PopoverMenuMod.Column) {
            popoverOption(EventScoutRoute(slug!!), "Post Event")
            popoverOption(LocationScoutRoute(slug), "Post Location")
            popoverOption(MediaForgeRoute(slug), "Post Media")
        }
    }
}