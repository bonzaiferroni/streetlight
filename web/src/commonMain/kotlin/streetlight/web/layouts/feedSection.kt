package streetlight.web.layouts

import koala.SvgFile
import koala.modifier.*
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
    galaxyId: GalaxyId? = null,
    cityId: CityId? = null,
) {
    section {
        galaxyId?.let {
            setAttribute(AppAttribute.GalaxyId.to(it))
        }
        cityId?.let {
            setAttribute(AppAttribute.CityId.to(it))
        }

        // the spacer matches the switch, keeping the heading centered
        row(AlignItemsCenter) {
            div(FeedSection.SwitchWidth)
            filigree(Flex1) {
                heading2("Posts", SectionHeadingMod)
            }
            rootSwitch(FeedRow.Mode, modify(FeedSection.SwitchWidth, JustifyContentEnd)) { icon(it.toSvg()) }
        }

        feed.marks?.takeIf { it.size == 1 }?.values?.first()?.let { feedMarks ->
            row(JustifyContentCenter) {
                feedMarks.forEach { mark ->
                    textBlock(mark.name) {
                        setAttribute(AppAttribute.MarkId.to(mark.markId))
                        onClick = FeedSection.SortByMark.invokeJs(ThisElement)
                    }
                }
            }
        }

        layoutFeed {
            // td: message when empty
            feed.entities.forEach { entity ->
                val curator = feed.curatorOf(entity)
                feedRow(entity, galaxyId == null, curator)
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
    column(FeedSection.MountId, modify(FeedRow.Feed, FeedSection.FeedColumnMod)) {
        block()
    }
}

object FeedSection {
    val MountId = Id("feed-layout")

    val Attribute = slugAttributeOf("feed-slug")
    val FeedColumnMod = modify(Gap2Px, MoonShadow)
    val NextCursor = jsonAttributeOf<EntityCursor>("next-post-cursor")

    val SortByMark = JsSignature("sortByMark")
    val MorePosts = JsSignature("morePosts")

    val SwitchWidth = Width(12)
}

fun FeedMode.toSvg() = when (this) {
    FeedMode.Minimal -> SvgFile.LayoutMinimal
    FeedMode.Row -> SvgFile.LayoutRow
    FeedMode.Grid -> SvgFile.LayoutGrid
}

// language="CSS"
val FeedSectionCss get() = """
${rootSwitchCss(FeedRow.Mode, FeedMode.entries)}
"""