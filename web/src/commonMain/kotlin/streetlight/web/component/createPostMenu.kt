package streetlight.web.component

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.page.PopoverId
import koala.component.*

/** Opens the create post menu, for posts to [galaxy] when given. */
fun FlowContent.createPostMenu(galaxy: Galaxy? = null) {
    button("Create Post", Zen, "☰") {
        setPopoverTarget(PopoverId.CreatePost)
        galaxy?.let {
            setAttribute(Attribute.Slug.to(galaxy.slug))
        }
    }
}