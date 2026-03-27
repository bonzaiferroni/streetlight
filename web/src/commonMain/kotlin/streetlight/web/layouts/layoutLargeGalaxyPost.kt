package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.SvgFile
import koala.css.*
import koala.html.TagAttribute
import koala.html.action
import koala.html.actionIfNotNull
import koala.html.box
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.heading5
import koala.html.icon
import koala.html.imageWithBackdrop
import koala.html.row
import koala.html.setData
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.GalaxyPost
import streetlight.model.data.StarType

fun FlowContent.layoutLargeGalaxyPost(post: GalaxyPost) {
    val postRoute = post.route ?: return

    card(modify(Padding0, OverflowHidden)) {
        column(modify(QueryLargeRow, AlignItemsStretch, Gap0)) {

            // non-grid content
            column(modify(Flex3, QueryMediumRow, AlignItemsStretch, Gap0)) {
                post.imageUrl?.let { imageUrl ->
                    imageWithBackdrop(imageUrl, modify(Flex1, MinHeight24, AlignSelfStretch))
                }
                column(modify(Flex2, Padding1, AlignItemsStretch, MaxHeight24)) {
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
                        box(modify(Flex1, SmallFont, OverflowHidden, FadeBottom)) {
                            action(postRoute) {
                                textBlock(description)
                            }
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
            row(modify(Flex1, QueryLargeColumn, MinHeight8, FlexItems1, AlignItemsStretch, GapTiny, TextAlignCenter, WrapFlex, SmallFont)) {
                val cellModifiers = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter, MinWidth16)
                card(cellModifiers) {
                    post.event?.startsAt?.let { startsAt ->
                        row(modify(WrapFlex, JustifyContentCenter, Gap0, AlignItemsCenter)) {
                            heading5(startsAt.toRelativeDayFormat())
                            textBlock("8:00 PM", modify(MarginLeft1))
                        }
                    }
                }
                card(cellModifiers) {
                    post.event?.cost?.let { cost ->
                        val ticketsUrl = cost.takeIf { it != 0f }?.let {
                            post.event?.url
                        }
                        actionIfNotNull(ticketsUrl) {
                            row(modify(WrapFlex, JustifyContentCenter, Gap0)) {
                                textBlock("tickets:", modify(Dim))
                                textBlock("$$cost", modify(MarginLeft1))
                            }
                        }
                    }
                }
                card(cellModifiers) {
                    row(modify(WrapFlex, JustifyContentCenter, Gap0)) {
                        textBlock("from:", modify(Dim))
                        textBlock(post.username ?: "anonymous", modify(MarginLeft1))
                    }
                }
                post.event?.let { event ->
                    card(cellModifiers) {
                        starCell(event.eventId)
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