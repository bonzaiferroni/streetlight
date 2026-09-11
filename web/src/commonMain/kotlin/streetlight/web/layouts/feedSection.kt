package streetlight.web.layouts

import koala.css.*
import koala.html.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod
import streetlight.web.ui.AppAttribute

fun FlowContent.feedSection(
    feed: EntityFeed,
    isUniverse: Boolean,
) {
    section {
        feed.marks?.keys?.firstOrNull()?.let {
            setAttribute(AppAttribute.GalaxyId.to(it))
        }

        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        feed.marks?.takeIf { it.size == 1 }?.values?.first()?.let { feedMarks ->
            row(modify(JustifyContentCenter)) {
                feedMarks.forEach { mark ->
                    textBlock(mark.name) {
                        setAttribute(AppAttribute.MarkId.to(mark.markId))
                        onClick = FeedSection.SortByMark.invokeJs(ThisElement)
                    }
                }
            }
        }

        layoutFeed {
            feed.entities.forEach { entity ->
                val curator = feed.curatorOf(entity)
                feedRow(entity, isUniverse, curator)
            }

            feed.nextCursor?.let {
                button("more") {
                    setAttribute(FeedSection.NextCursor.to(it))
                    onClick = FeedSection.MorePosts.invokeJs(ThisElement)
                }
            }
        }
    }
}

fun FlowContent.layoutFeed(
    block: FlowContent.() -> Unit
) {
    column(FeedSection.MountId, FeedSection.FeedColumnMod) {
        block()
    }
}

object FeedSection {
    val MountId = Id("feed-layout")

    val Attribute = slugAttributeOf("feed-slug")
    val FeedColumnMod = modify(Gap2Px, BorderRadius2, OverflowClip, MoonShadow)
    val NextCursor = jsonAttributeOf<PostCursor>("next-post-cursor")

    val SortByMark = JsSignature("sortByMark")
    val MorePosts = JsSignature("morePosts")
}