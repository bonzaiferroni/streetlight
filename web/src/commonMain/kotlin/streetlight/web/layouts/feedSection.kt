package streetlight.web.layouts

import koala.modifier.*
import koala.html.*
import koala.interop.JsSignature
import koala.interop.KoalaFun
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

        filigree {
            heading2("Posts", SectionHeadingMod)
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

        feedModeControl()

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

fun FlowContent.feedModeControl() {
    row(JustifyContentCenter) {
        FeedSection.ModeOptions.forEach { mode ->
            button(mode.name, Zen) {
                setAttribute(FeedSection.ModeOption.to(mode))
                onClick = KoalaFun.applyRootAttribute.invokeJs(FeedRow.Mode.identifier, mode.name)
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

    val ModeOption = enumAttributeOf<FeedMode>("feed-mode-option")
    val ModeOptions = FeedMode.entries
}

// the option matching the root's mode is marked
//language="CSS"
val FeedSectionCss get() = with(FeedSection) {
    ModeOptions.joinToString("\n") { mode ->
        "${FeedRow.Mode.selector(mode)} ${ModeOption.selector(mode)} { outline: 2px solid rgb(var(--primary)); }"
    }
}