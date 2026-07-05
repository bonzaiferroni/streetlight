package streetlight.web.shells

import kampfire.model.large
import kampfire.model.small
import kampfire.model.toUrl
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.web.EventUpdateRoute
import streetlight.web.layouts.costCell
import streetlight.web.layouts.starCell
import streetlight.web.layouts.startsAtCell
import streetlight.web.pages.appFooter
import streetlight.web.ui.featureHeader
import streetlight.web.ui.starLightCell

fun FlowContent.eventShell(event: EventLocation) {
    column(EventProfileKey.id, modify(AlignItemsStretch, Gap4, MarginTop1)) {
        featureHeader(
            title = event.title,
            descriptor = "at",
            subtitle = event.locationName,
            image = event.images.large,
            description = event.description,
            cellContent = {
                startsAtCell(event.startsAt)
                costCell(event.cost, event.url?.toUrl())
                starCell(event.scout)
                starLightCell(event)
            },
            links = event.links,
            editRoute = EventUpdateRoute(event.eventSlug),
        )

        tabs {
            tab("Location") {
                column {
                    card(modify(QueryContainer, ZenBg, BorderRadius2, Padding0, OverflowClip, Gap0)) {
                        column(modify(ContainerMdRow, FlexItems1, CardBg, Gap0)) {
                            geoMapMount(event.geoPoint, modify(MinHeight48))
                            column(modify(JustifyContentCenter, AlignItemsCenter)) {
                                event.locationImages.small?.let {
                                    image(it, modify(Flex1, BorderRadius2, MaxHeight16))
                                }
                                column(modify(PaddingX1, PaddingY2, Gap0)) {
                                    heading2(event.locationName, modify(TextAlignCenter, MinWidth0))
                                    event.addressLine?.let {
                                        filigree {
                                            heading4(it, modify(OpacityHigh, TextAlignCenter))
                                        }
                                    }
                                }
                            }
                        }

                        column(modify(ContainerMdRow, Padding4, Gap4, AlignItemsStart)) {
                            column(modify(Flex4)) {
                                event.locationDescription?.let {
                                    markdown(it)
                                }
                            }
                        }
                    }
                }
            }
            tab("Comments") {
                textBlock("coming soon")
            }
        }
        appFooter(EventProfileKey.SOURCE)

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

object EventProfileKey {
    val id = Id("event-profile")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/eventShell.kt"
}