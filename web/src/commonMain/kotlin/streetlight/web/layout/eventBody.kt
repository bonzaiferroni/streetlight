package streetlight.web.layout

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import koala.component.*
import koala.layout.*
import koala.markdown.*
import streetlight.model.data.costDetailOf

/** The page of [event]: its header, and its location and comments in tabs. */
fun FlowContent.eventBody(event: EventLocation) {
    entityHeader(
        entity = event,
        descriptor = "at",
        cells = listOfNotNull(
            event.startsAt?.let { startsAtCell(it) },
            event.cost?.let { costDetailOf(it, event.url) },
            starCell(event.host),
        ),
    )

    tabs {
        tab("Location") {
            column {
                card(modify(ContainerTypeInlineSize, ZenBg, BorderRadius2, Padding(0), OverflowClip, Gap0)) {
                    column(modify(ContainerMdRow, FlexItems1, CardBg, Gap0)) {
                        geoMapMount(event.geoPoint, MinHeight(48))
                        column(modify(JustifyContentCenter, AlignItemsCenter)) {
                            event.locationImage?.small?.let {
                                image(it, modify(Flex1, BorderRadius2, MaxHeight(16)))
                            }
                            column(modify(PaddingX1, PaddingY2, Gap0)) {
                                heading2(event.locationName, modify(TextAlignCenter, MinWidth(0)))
                                event.addressLine?.let {
                                    filigree {
                                        heading4(it, modify(OpacityHigh, TextAlignCenter))
                                    }
                                }
                            }
                        }
                    }

                    column(modify(ContainerMdRow, Padding(4), Gap(4), AlignItemsStart)) {
                        column(Flex4) {
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
}
