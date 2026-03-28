package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.SvgFile
import koala.css.*
import koala.html.TagAttribute
import koala.html.action
import koala.html.actionIfNotNull
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.icon
import koala.html.fillImage
import koala.html.row
import koala.html.setData
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyPost
import streetlight.model.data.StarType

fun FlowContent.largePostCard(post: GalaxyPost) {
    val postRoute = post.route ?: return

    card(modify(QueryContainer, Padding0, OverflowHidden)) {
        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                fillImage(post.imageUrl, modify(Flex1, MinHeight24))
                column(modify(Flex2, Padding1, Height24, MaxHeight24)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            action(postRoute) {
                                heading3(post.title, modify(SingleLine, LineHeight1, MarginTop1))
                            }
                            post.location?.let { location ->
                                action(location.route) {
                                    textBlock("${location.name}, ${location.city}", modify(Dim))
                                }
                            }
                        }
                    }
                    post.description?.let { description ->
                        action(postRoute, modify(Flex1, SmallText, OverflowHidden, FadeBottom)) {
                            textBlock(description)
                        }
                    }

                    row {
                        post.event?.url?.let { url ->
                            btn("source", url)
                        }
                        post.event?.links?.forEach { link ->
                            btn(link.label, link.url)
                        }
                    }
                }
            }

            // grid content
            row(modify(Flex1, ContainerLgColumn, MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex)) {
                val cellMods = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16)
                val rowMods = modify(JustifyContentCenter)
                card(cellMods) {
                    post.event?.startsAt?.let { startsAt ->
                        row(rowMods) {
                            textBlock(startsAt.toRelativeDayFormat(), modify(Bold))
                            textBlock("8:00 PM")
                        }
                    }
                }
                card(cellMods) {
                    post.event?.cost?.let { cost ->
                        val ticketsUrl = cost.takeIf { it != 0f }?.let {
                            post.event?.url
                        }
                        actionIfNotNull(ticketsUrl) {
                            row(rowMods) {
                                textBlock("tickets:", modify(Dim))
                                textBlock("$$cost")
                            }
                        }
                    }
                }
                card(cellMods) {
                    row(rowMods) {
                        textBlock("from:", modify(Dim))
                        textBlock(post.username ?: "anonymous")
                    }
                }
                post.event?.let { event ->
                    card(cellMods) {
                        row(rowMods) {
                            textBlock((0..10).random().toString())
                            starCell(event.eventId)
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.starCell(eventId: EventId) {
    row(modify(WidthAuto)) {
        setData(EventKey.StarEventId, eventId)
        // textBlock(post.visibility.toString())
        icon(SvgFile.LoaderSmall, modify(Height3, AspectRatio1))
    }
}

object EventKey {
    val StarEventId = TagAttribute<EventId>("event-id")
    // val StarClass = Css("event-star")
}

val StarType?.iconPath get() = when(this) {
    StarType.Star -> SvgFile.StarFilled
    StarType.Calendar -> SvgFile.StarFilled // td: handle differently
    null -> SvgFile.StarOutline
}