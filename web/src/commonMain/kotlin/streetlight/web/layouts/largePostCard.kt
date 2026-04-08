package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.medium
import koala.SiteImage
import koala.css.*
import koala.html.*
import kotlin.time.Instant
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.ExtraLink
import streetlight.model.data.GalaxyId
import streetlight.web.StreetlightRoute
import streetlight.web.ui.StarLightKey
import streetlight.web.ui.starLight

fun FlowContent.largePostCard(
    title: String,
    subtitle: String?,
    description: String?,
    sourceUrl: String?,
    links: List<ExtraLink>?,
    images: ScaledImageArray?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute?,
    modifiers: ModifierSet? = null,
    cells: List<(FlowContent.() -> Unit)?>
) {
    val imageUrl = images?.medium ?: SiteImage.placeholder.url

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

                            subtitle?.let {
                                fun FlowContent.showSubtitle() = textBlock(subtitle, modify(Dim))
                                when (subRoute) {
                                    null -> showSubtitle()
                                    else -> action(subRoute) { showSubtitle() }
                                }
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

fun FlowContent.lightCell(galaxyId: GalaxyId) {
    row(modify(WidthAuto)) {
        setData(StarLightKey.GalaxyLightId, galaxyId)
        starLight((0..10).random())
    }
}

fun FlowContent.lightCell(eventId: EventId) {
    row {
        setData(StarLightKey.EventLightId, eventId)
        starLight((0..10).random())
    }
}

