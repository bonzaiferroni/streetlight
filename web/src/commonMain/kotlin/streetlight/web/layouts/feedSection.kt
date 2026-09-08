package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.feedSection(
    entities: List<FeedEntity>,
    isUniverse: Boolean,
    feedMarks: Map<GalaxyId, List<FeedMark>>? = null,
    postMarks: Map<PostId, List<PostMark>>? = null,
) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        layoutFeed {
            entities.forEach { entity ->
                val curator = run {
                    val postId = entity.post?.postId ?: return@run null
                    val galaxyId = entity.post?.galaxy?.galaxyId ?: return@run null
                    val postMarks = postMarks ?: return@run null
                    val rowFeedMarks = feedMarks?.get(galaxyId) ?: return@run null
                    curatorStatusOf(postId, rowFeedMarks, postMarks[postId])
                }
                feedRow(entity, isUniverse, curator)
            }
        }
    }
}

fun FlowContent.layoutFeed(block: FlowContent.() -> Unit) {
    mount(FeedKey.PostLayoutId) {
        column(modify(Gap2Px, BorderRadius2, OverflowClip, MoonShadow)) {
            block()
        }
    }
}

object FeedKey {
    val PostLayoutId = Id("feed-layout")

    val Attribute = slugAttributeOf("feed-slug")
}