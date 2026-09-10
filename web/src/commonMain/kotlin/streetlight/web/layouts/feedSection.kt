package streetlight.web.layouts

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.feedSection(
    entities: List<FeedEntity>,
    isUniverse: Boolean,
    marks: Map<GalaxyId, List<Mark>>? = null,
    statusMap: Map<PostId, List<MarkStatus>>? = null,
) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        marks?.takeIf { it.size == 1 }?.values?.first()?.let { feedMarks ->
            row(modify(JustifyContentCenter)) {
                feedMarks.forEach {
                    textBlock(it.name)
                }
            }
        }

        layoutFeed {
            entities.forEach { entity ->
                val curator = run {
                    val postId = entity.post?.postId ?: return@run null
                    val galaxyId = entity.post?.galaxy?.galaxyId ?: return@run null
                    val postMarks = statusMap ?: return@run null
                    val rowFeedMarks = marks?.get(galaxyId) ?: return@run null
                    curatorStatusOf(postId, rowFeedMarks, postMarks[postId])
                }
                feedRow(entity, isUniverse, curator)
            }
        }
    }
}

fun FlowContent.layoutFeed(
    block: FlowContent.() -> Unit
) {
    column(FeedKey.PostLayoutId, modify(Gap2Px, BorderRadius2, OverflowClip, MoonShadow)) {
        block()
    }
}

object FeedKey {
    val PostLayoutId = Id("feed-layout")

    val Attribute = slugAttributeOf("feed-slug")
}