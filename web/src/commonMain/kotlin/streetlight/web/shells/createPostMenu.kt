package streetlight.web.shells

import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.ui.PopoverId

/** Opens the create post menu, for posts to [galaxy] when given. */
fun FlowContent.createPostMenu(galaxy: Galaxy? = null) {
    button("Create Post", Accent, "☰") {
        setPopoverTarget(PopoverId.CreatePost)
        galaxy?.let {
            setAttribute(Attribute.Slug.to(galaxy.slug))
        }
    }
}