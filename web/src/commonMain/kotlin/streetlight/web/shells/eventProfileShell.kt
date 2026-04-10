package streetlight.web.shells

import kampfire.model.medium
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.web.EditEventIdRoute

fun FlowContent.eventProfileShell(event: EventLocation) {
    column(EventProfileShell.id, modify(AlignItemsStretch, QueryContainer)) {
        card(modify(ZenCardBg, BorderRadius2, Padding0, OverflowClip, Gap0)) {
            column(modify(ContainerMdRow, FlexItems1, CardBg)) {
                val imageUrl = event.images.medium
                if (imageUrl != null) {
                    featureImage(imageUrl, modify(MaxHeight64))
                }
                column(modify(JustifyContentCenter, PaddingX1, PaddingY2)) {
                    heading2(event.title, modify(TextAlignCenter))
                    filigree {
                        textBlock("at", modify(OpacityHalf))
                    }
                    heading4(event.locationName, modify(OpacityMost, TextAlignCenter))
                }
            }
            column(modify(Padding4)) {
                event.description?.let {
                    markdownContent(it)
                }
            }
        }

//        tabs(EventProfileShell.tabsId) {
//            tab("Profile") {
//                textBlock("[Event information]")
//                btn("edit", EditEventIdRoute(event.eventId))
//            }
//            tab("Requests") {
//                textBlock("[Requests information]")
//            }
//        }
    }
}

object EventProfileShell {
    val id = Id("event-profile")
    val tabsId = Id("event-tabs")
}