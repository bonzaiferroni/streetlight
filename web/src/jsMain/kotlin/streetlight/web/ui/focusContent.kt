package streetlight.web.ui

import kampfire.model.small
import koala.css.*
import koala.dom.*
import koala.html.featureImage
import koala.html.heading3
import koala.html.row
import streetlight.model.data.EventPost
import streetlight.web.layouts.cellCard
import streetlight.web.layouts.costCell
import streetlight.web.layouts.lightCell
import streetlight.web.layouts.postedByCell
import streetlight.web.layouts.startsAtCell

fun RenderContext.eventFocusContent(post: EventPost) {
    val event = post.event

    card(modify(Padding0, Gap0, BlurBackdrop, OverflowClip)) {
        row(modify(Gap0, Height16)) {
            featureImage(post.images.small, modify(Flex1, Aspect3By2))
            column(modify(Padding1, Flex2)) {
                column {
                    heading3(event?.title ?: "[removed]", modify(SingleLine))
                    event?.locationName?.let {
                        textBlock(it, modify(OpacityMost))
                    }
                }
                event?.description?.let {
                    textBlock(it, modify(Flex1, MinHeight0, FadeBottom))
                }
            }
        }
        val event = event ?: return@card
        row(modify(Flex1, MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex, MoonShadow)) {
            cellCard {
                startsAtCell(event.startsAt)
            }
            cellCard {
                costCell(event.cost, event.url)
            }
            cellCard {
                postedByCell(event.username)
            }
            cellCard {
                lightCell(event.eventId)
            }
        }
    }
}