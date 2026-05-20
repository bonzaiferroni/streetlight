package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.postSection(posts: List<Post>) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        mount(PostKey.PostLayoutId) {
            column(PostKey.PostLayoutColumnMod) {
                layoutPosts(posts)
            }
        }
    }
}

fun FlowContent.layoutPosts(posts: List<Post>) {
    posts.forEach { post ->
        postCardOf(post)
    }
}

object PostKey {
    val PostMenuId = Id("post-menu")
    val PostLayoutId = Id("post-layout")
    val PostLayoutColumnMod = modify(Gap2)

    val Attribute = uuidAttributeOf("post-id") { PostId(it) }
}