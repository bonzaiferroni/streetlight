package streetlight.web.ui

import kampfire.api.Slug
import koala.dom.*
import koala.html.LabeledRoute
import koala.modifier.Attribute
import koala.modifier.getAttribute
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.MediaForgeRoute

/** The create post menu, posting to the galaxy of the button that opened it. */
fun ViewScope.wireCreatePost() {
    popoverMenu(
        popoverId = PopoverId.CreatePost,
        transform = { it.getAttribute(Attribute.Slug) },
        defaultValue = Slug.Empty
    ) { value ->
        val slug = value.takeIf { it.value.isNotEmpty() }
        column(PopoverMenuMod.Column) {
            popoverOption(LabeledRoute(EventScoutRoute(slug), "Post Event"))
            popoverOption(LabeledRoute(LocationScoutRoute(slug), "Post Location"))
            popoverOption(LabeledRoute(MediaForgeRoute(slug), "Post Media"))
        }
    }
}