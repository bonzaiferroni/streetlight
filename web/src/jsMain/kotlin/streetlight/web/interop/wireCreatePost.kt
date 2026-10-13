package streetlight.web.interop

import kampfire.api.Slug
import koala.dom.*
import koala.model.LabeledRoute
import koala.modifier.Attribute
import koala.modifier.getAttribute
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.model.ui.MediaForgeRoute
import koala.dom.component.*
import streetlight.web.page.PopoverId
import streetlight.web.dom.component.PopoverMenuMod
import streetlight.web.dom.component.popoverMenu

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