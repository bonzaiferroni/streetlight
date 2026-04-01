package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.SvgFile
import koala.css.*
import koala.html.Attribute
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
import streetlight.model.data.EventPost
import streetlight.model.data.StarType

fun FlowContent.largePostCard(post: EventPost) {
    val postRoute = post.route ?: return
    val event = post.event

    card(modify(QueryContainer, Padding0, OverflowHidden)) {
        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                fillImage(post.imageUrl, modify(Flex1, MinHeight24))
                column(modify(Flex2, Padding1, Height24, MaxHeight24)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            action(postRoute) {
                                heading3(post.title, modify(WhiteSpaceNoWrap, LineHeight1, MarginTop1, TextOverflowHidden))
                            }
                            val location = post.location
                            action(location.route) {
                                textBlock("${location.name}, ${location.city}", modify(Dim))
                            }
                        }
                    }
                    post.description?.let { description ->
                        action(postRoute, modify(Flex1, SmallText, OverflowHidden, FadeBottom)) {
                            textBlock(description)
                        }
                    }

                    row {
                        post.event.url?.let { url ->
                            btn("source", url)
                        }
                        post.event.links?.forEach { link ->
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
                    row(rowMods) {
                        textBlock(post.event.startsAt.toRelativeDayFormat(), modify(Bold))
                        textBlock("8:00 PM")
                    }
                }
                card(cellMods) {
                    val cost = post.event.cost
                    val ticketsUrl = cost.takeIf { it != 0f }?.let {
                        post.event.url
                    }
                    actionIfNotNull(ticketsUrl) {
                        row(rowMods) {
                            textBlock("tickets:", modify(Dim))
                            textBlock("$$cost")
                        }
                    }
                }
                card(cellMods) {
                    row(rowMods) {
                        textBlock("from:", modify(Dim))
                        textBlock(post.username ?: "anonymous")
                    }
                }
                card(cellMods) {
                    starCell(event.eventId)
                }
            }
        }
    }
}

fun FlowContent.starCell(eventId: EventId) {
    row {
        setData(EventKey.EventStarId, eventId)
        textBlock((0..10).random().toString())
        icon(SvgFile.LoaderSmall, modify(Height3, AspectRatio1))
    }
}

object EventKey {
    val EventStarId = Attribute<EventId>("event-star-id")
    // val StarClass = Css("event-star")
}

val StarType?.iconPath get() = when(this) {
    StarType.Star -> SvgFile.StarFilled
    StarType.Calendar -> SvgFile.StarFilled // td: handle differently
    null -> SvgFile.StarOutline
}