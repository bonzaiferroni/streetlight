package streetlight.web.shells

import kampfire.model.medium
import kampfire.model.small
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.web.EditEventIdRoute
import streetlight.web.layouts.cellCard
import streetlight.web.layouts.costCell
import streetlight.web.layouts.lightCell
import streetlight.web.layouts.postedByCell
import streetlight.web.layouts.startsAtCell
import streetlight.web.pages.appFooter

fun FlowContent.eventProfileShell(event: EventLocation) {
    column(EventProfileKey.id, modify(AlignItemsStretch, Gap4)) {
        card(modify(ZenCardBg, BorderRadius2, Padding0, OverflowClip, Gap0, QueryContainer)) {
            column(modify(ContainerMdRow, FlexItems1, CardBg, Gap0)) {
                val imageUrl = event.images.medium
                if (imageUrl != null) {
                    featureImage(imageUrl, modify(Aspect3By2))
                }
                column(modify(JustifyContentCenter)) {
                    column(modify(PaddingX1, PaddingY2)) {
                        heading2(event.title, modify(TextAlignCenter, MinWidth0))
                        filigree(modify()) {
                            textBlock("at", modify(OpacityHalf))
                        }
                        heading4(event.locationName, modify(OpacityMost, TextAlignCenter))
                    }
                }
            }
            row(modify(MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex, ZenCardBg)) {
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
            column(modify(ContainerMdRow, Padding4, Gap4, AlignItemsStart)) {
                column(modify(Flex4)) {
                    event.description?.let {
                        markdown(it)
                    }
                }
                event.links?.let { links ->
                    row(modify(Flex1, WrapFlex, AlignItemsStart, FlexItems1)) {
                        links.forEach { link ->
                            btn(link.label, link.url)
                        }
                        btn("edit", EditEventIdRoute(event.eventId))
                    }
                }
            }
        }

        tabs {
            tab("Location") {
                column {
                    card(modify(QueryContainer, ZenCardBg, BorderRadius2, Padding0, OverflowClip, Gap0)) {
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
                                            heading4(it, modify(OpacityMost, TextAlignCenter))
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

//                            event.links?.let { links ->
//                                row(modify(Flex1, WrapFlex, AlignItemsStart, FlexItems1)) {
//                                    links.forEach { link ->
//                                        btn(link.label, link.url)
//                                    }
//                                }
//                            }
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
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/eventProfileShell.kt"
}