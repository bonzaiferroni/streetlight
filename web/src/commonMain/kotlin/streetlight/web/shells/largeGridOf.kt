package streetlight.web.shells

import kabinet.utils.toRelativeDayFormat
import koala.css.*
import koala.html.button
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.heading4
import koala.html.icon
import koala.html.imageWithBackdrop
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyPost
import streetlight.web.ui.SvgPath

fun FlowContent.largeGridOf(post: GalaxyPost) {
    card(modify(Padding0, OverflowHidden)) {
        column(modify(QueryLargeRow, AlignItemsStretch, Gap0)) {

            // non-grid content
            column(modify(Flex2, QueryMediumRow, AlignItemsStart, Padding1)) {
                post.imageUrl?.let { imageUrl ->
                    imageWithBackdrop(imageUrl, modify(Flex1, MinHeight16, BorderRadius1, AlignSelfStretch))
                }
                column(modify(Flex2)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            heading3(post.title)
                            post.location?.let { location ->
                                textBlock(location.name, modify(Dim))
                            }
                        }
                    }
                    post.description?.let { description ->
                        textBlock(description, modify(Flex1, MaxHeight8, SmallFont, OverflowHidden))
                    }
                    row {
                        button("their music")
                        button("signup rules")
                        button("buy tickets")
                    }
                }
            }

            // grid content
            row(modify(Flex1, MinHeight8, FlexItems1, AlignItemsStretch, GapTiny, TextAlignCenter, WrapFlex)) {
                val cellModifiers = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyCenter, MinWidth16)
                card(cellModifiers) {
                    post.location?.let { location ->
                        textBlock(location.name)
                    }
                }
                card(cellModifiers) {
                    post.event?.startsAt?.let { startsAt ->
                        heading4(startsAt.toRelativeDayFormat())
                        textBlock("8:00 PM")
                    }
                }
                card(cellModifiers) {
                    post.event?.cost?.let {
                        textBlock("Tickets", modify(Dim, SmallFont))
                        textBlock("$$it")
                    }
                }
                card(cellModifiers) {
                    row(modify(WidthAuto)) {
                        textBlock("31")
                        icon(SvgPath.starOutline, modify(Height100, Square))
                    }
                }
            }
        }
    }
}