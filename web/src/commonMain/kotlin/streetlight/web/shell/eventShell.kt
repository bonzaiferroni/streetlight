package streetlight.web.shell

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventUpdateRoute
import streetlight.web.layout.costCell
import streetlight.web.layout.starCell
import streetlight.web.layout.startsAtCell
import streetlight.web.layout.mainBody
import streetlight.web.layout.entityHeader
import koala.component.*
import koala.layout.*
import koala.markdown.*
import koala.page.*

fun FlowContent.eventShell(event: EventLocation) {
    mainBody("eventShell.kt") {
        entityHeader(
            entity = event,
            descriptor = "at",
            cells = listOfNotNull(
                event.startsAt?.let { startsAtCell(it) },
                event.cost?.let { costCell(it, event.url) },
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

    dataIsland(EventShell.island, event)
}

object EventShell {
    val island = Id("event-shell-island")
}