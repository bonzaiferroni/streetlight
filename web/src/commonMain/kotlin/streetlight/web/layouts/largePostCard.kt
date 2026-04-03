package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toRelativeTimeFormat
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.datetime.Instant
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.EventPost
import streetlight.model.data.ExtraLink
import streetlight.model.data.GalaxyId
import streetlight.model.data.ProjectId
import streetlight.web.StreetlightRoute

fun FlowContent.largePostCard(
    title: String,
    subtitle: String,
    description: String?,
    sourceUrl: String?,
    links: List<ExtraLink>?,
    imageUrl: String?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute,
    modifiers: ModifierSet? = null,
    cells: List<(FlowContent.() -> Unit)?>
) {
    val imageUrl = imageUrl ?: SiteImage.placeholder

    card(modify(modifiers, QueryContainer, Padding0, OverflowHidden)) {
        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                fillImage(imageUrl, modify(Flex1, MinHeight24))
                column(modify(Flex2, Padding1, Height24, MaxHeight24)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            action(postRoute) {
                                heading3(title, modify(WhiteSpaceNoWrap, LineHeight1, MarginTop1, TextOverflowHidden))
                            }
                            action(subRoute) {
                                textBlock(subtitle, modify(Dim))
                            }
                        }
                    }
                    description?.let { description ->
                        action(postRoute, modify(Flex1, SmallText, OverflowHidden, FadeBottom)) {
                            textBlock(description)
                        }
                    }

                    row {
                        sourceUrl?.let { url ->
                            btn("source", url)
                        }
                        links?.forEach { link ->
                            btn(link.label, link.url)
                        }
                    }
                }
            }

            // grid content
            row(modify(Flex1, ContainerLgColumn, MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex)) {
                val cellMods = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16)
                cells.forEach {
                    val cell = it ?: return@forEach
                    card(cellMods) {
                        cell()
                    }
                }
            }
        }
    }
}

object PostCard {
    val RowMod = modify(JustifyContentCenter)
}

fun FlowContent.startsAtCell(startsAt: Instant) {
    row(PostCard.RowMod) {
        textBlock(startsAt.toRelativeDayFormat(), modify(Bold))
        textBlock(startsAt.toTimeFormat())
    }
}

fun FlowContent.costCell(cost: Float, purchaseUrl: String?) {
    val ticketsUrl = cost.takeIf { it != 0f }?.let {
        purchaseUrl
    }
    actionIfNotNull(ticketsUrl) {
        row(PostCard.RowMod) {
            textBlock("tickets:", modify(Dim))
            textBlock("$$cost")
        }
    }
}

fun FlowContent.postedBy(username: String?) {
    row(PostCard.RowMod) {
        textBlock("from:", modify(Dim))
        textBlock(username ?: "someone")
    }
}

fun FlowContent.starCell(galaxyId: GalaxyId) {
    row(modify(WidthAuto)) {
        setData(GalaxyKey.GalaxyStarId, galaxyId)
        starCellContent((0..10).random())
    }
}

fun FlowContent.starCell(eventId: EventId) {
    row {
        setData(EventKey.EventStarId, eventId)
        starCellContent((0..10).random())
    }
}

private fun FlowContent.starCellContent(count: Int) {
    textBlock(count.toString())
    icon(SvgFile.LoaderSmall, modify(Height3, AspectRatio1))
}