package streetlight.web.shells

import kabinet.utils.toRelativeDayFormat
import koala.css.*
import koala.html.card
import koala.html.column
import koala.html.heading5
import koala.html.icon
import koala.html.image
import koala.html.row
import koala.html.spacer
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyPost
import streetlight.web.ui.SvgPath

fun FlowContent.gridOf(post: GalaxyPost) {
    // thumbnail
    // title/description

    // location/distance
    // time interval
    // price/availability
    // visibility/interest/comments
    val thumbUrl = post.thumbUrl
    val title = post.title
    val description = post.description
    card(modify(Padding0, OverflowHidden)) {
        column(modify(QueryRowLarge, AlignItemsStretch, Gap0)) {
            row(modify(Height16, AlignItemsStart, Padding1)) {
                thumbUrl?.let {
                    image(thumbUrl, modify(Height100, Square, BorderRadius1))
                }
                column(modify(Flex1, Height100)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            heading5(title)
                            post.location?.let { location ->
                                textBlock(location.name, modify(Dim))
                            }
                        }
                        icon(SvgPath.focus, modify(Height100, Square, Dim))
                    }
                    description?.let {
                        textBlock(description, modify(Flex1, SmallFont, Height2, OverflowHidden))
                    }
                }
            }
            row(modify(MinHeight8, FlexItems1, AlignItemsStretch, GapTiny, TextAlignCenter)) {
                val cellModifiers = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyCenter)
//                card(cellModifiers) {
//                    post.location?.let { location ->
//                        textBlock(location.name)
//                        textBlock("5.2 miles", modify(Dim))
//                    }
//                }
                card(cellModifiers) {
                    post.event?.startsAt?.let { startsAt ->
                        textBlock(startsAt.toRelativeDayFormat(), modify(LargeFont))
                        textBlock("8:00 PM")
                    }
                }
                card(cellModifiers) {
                    textBlock("Tickets", modify(Dim, SmallFont))
                    textBlock("$10")
                    post.event?.cost?.let {

                    }
                }
                card(cellModifiers) {
                    row(modify(WidthAuto, Gap0)) {
                        textBlock("31")
                        icon(SvgPath.starOutline, modify(Height100, Square))
                    }
                }
            }
        }
    }
}