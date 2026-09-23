package streetlight.web.layouts

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Post
import streetlight.web.ui.postMenu
import streetlight.web.ui.starToggle

fun buttonCellsOf(location: Location, post: Post? = null): FlowContent.() -> Unit = {
    buttonsCell {
        starToggle(location)
        moreButton()
        post?.let {
            postMenu(post.postId, post.username)
        }
    }
}

fun buttonCellsOf(event: EventLocation, showMore: Boolean, post: Post? = null): FlowContent.() -> Unit = {
    buttonsCell(MinWidth(32)) {
        starToggle(event)
        if (showMore) {
            moreButton()
        }
        post?.let {
            postMenu(post.postId, post.username)
        }
    }
}

fun buttonCellsOf(event: Event): FlowContent.() -> Unit = {
    buttonsCell {
        starToggle(event)
        moreButton()
    }
}

fun buttonCellsOf(galaxy: Galaxy): FlowContent.() -> Unit = {
    buttonsCell {
        starToggle(galaxy)
    }
}
