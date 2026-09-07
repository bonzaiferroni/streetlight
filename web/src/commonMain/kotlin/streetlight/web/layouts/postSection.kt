package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.postSection(
    posts: List<FeedEntity>,
    showGalaxy: Boolean,
    feedMarks: Map<GalaxyId, List<FeedMark>>? = null,
    postMarks: Map<PostId, List<PostMark>>? = null,
) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        layoutPosts {
            posts.forEach { post ->
                val curator = run {
                    val postId = post.postId ?: return@run null
                    val galaxyId = post.galaxyId ?: return@run null
                    val postMarks = postMarks ?: return@run null
                    val rowFeedMarks = feedMarks?.get(galaxyId) ?: return@run null
                    curatorStatusOf(postId, rowFeedMarks, postMarks[postId])
                }
                postRow(post, showGalaxy, curator)
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