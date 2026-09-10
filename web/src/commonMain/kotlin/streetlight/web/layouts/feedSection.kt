package streetlight.web.layouts

import koala.css.*
import koala.html.*
import koala.interop.ThisElement
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.*
import streetlight.web.interop.AppFun
import streetlight.web.shells.SectionHeadingMod
import streetlight.web.ui.AppAttribute

fun FlowContent.feedSection(
    feed: EntityFeed,
    isUniverse: Boolean,
) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        feed.marks?.takeIf { it.size == 1 }?.values?.first()?.let { feedMarks ->
            val galaxyId = feed.marks?.keys?.firstOrNull() ?: return@let
            row(modify(JustifyContentCenter)) {
                setAttribute(AppAttribute.GalaxyId.to(galaxyId))
                feedMarks.forEach { mark ->
                    textBlock(mark.name) {
                        setAttribute(AppAttribute.MarkId.to(mark.markId))
                        onClick = AppFun.SortByMark.invokeJs(ThisElement)
                    }
                }
            }
        }

        layoutFeed {
            feed.entities.forEach { entity ->
                val curator = feed.curatorOf(entity)
                feedRow(entity, isUniverse, curator)
            }
        }
    }
}

fun FlowContent.layoutFeed(
    block: FlowContent.() -> Unit
) {
    mount(FeedSection.MountId) {
        column(FeedSection.FeedColumnMod) {
            block()
        }
    }
}

object FeedSection {
    val MountId = Id("feed-layout")

    val Attribute = slugAttributeOf("feed-slug")
    val FeedColumnMod = modify(Gap2Px, BorderRadius2, OverflowClip, MoonShadow)
}