package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod
import kotlin.uuid.Uuid

fun FlowContent.postSection(
    posts: List<FeedEntity>,
    showGalaxy: Boolean,
    feedMarks: List<FeedMark>? = null,
    postMarks: Map<Uuid, List<PostMark>>? = null,
) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        layoutPosts {
            posts.forEach { post ->
                postRow(post, showGalaxy, feedMarks, postMarks?.let { it[post.recordId] })
            }
        }
    }
}

fun FlowContent.layoutPosts(block: FlowContent.() -> Unit) {
    mount(PostKey.PostLayoutId) {
        column(modify(Gap2Px, BorderRadius2, OverflowClip, MoonShadow)) {
            block()
        }
    }
}

object PostKey {
    val PostLayoutId = Id("post-layout")

    val Attribute = slugAttributeOf("post-slug")
}