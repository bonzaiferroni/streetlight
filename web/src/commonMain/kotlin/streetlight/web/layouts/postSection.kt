package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.postSection(posts: List<GalaxyPost>) {
    section(modify(FeedPost.SmallRow)) {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        layoutPosts {
            posts.forEach { post ->
                postCardOf(post)
            }
        }
    }
}

fun FlowContent.layoutPosts(block: FlowContent.() -> Unit) {
    mount(PostKey.PostLayoutId) {
        column(modify(FeedPost.FeedColumn)) {
            block()
        }
    }
}

object PostKey {
    val PostLayoutId = Id("post-layout")

    val Attribute = slugAttributeOf("post-slug")
}