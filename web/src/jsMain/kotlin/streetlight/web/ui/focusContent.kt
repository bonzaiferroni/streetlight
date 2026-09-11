package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.featureImage
import koala.html.heading3
import koala.html.row
import streetlight.model.data.EventPost
import streetlight.web.layouts.cell
import streetlight.web.layouts.costCell
import streetlight.web.layouts.starCell
import streetlight.web.layouts.startsAtCell

fun ViewScope.eventFocusContent(post: EventPost) {
    val event = post.event

    card(modify(Padding0, Gap0, BlurBackdrop, OverflowClip)) {
        row(modify(Gap0, Height16)) {
            featureImage(post.image, modify(Flex1, Aspect3By2))
            column(modify(Padding1, Flex2)) {
                column {
                    heading3(event.title ?: "[removed]", modify(SingleLine))
                    event.locationName?.let {
                        textBlock(it, modify(OpacityHigh))
                    }
                }
                event.description?.let {
                    textBlock(it.value, modify(Flex1, MinHeight0, FadeBottom))
                }
            }
        }
        row(modify(Flex1, MinHeight8, FlexItems1, Gap2Px, TextAlignCenter, FlexWrap, MoonShadow)) {
            cell {
                startsAtCell(event.startsAt)
            }
            event.cost?.let {
                cell {
                    costCell(it, event.url)
                }
            }
            cell {
                starCell(event.scout)
            }
            cell {
                starToggle(event)
            }
        }
    }
}