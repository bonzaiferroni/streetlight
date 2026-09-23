package streetlight.web.shells

import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventUpdateRoute
import streetlight.web.layouts.costCell
import streetlight.web.layouts.starCell
import streetlight.web.layouts.startsAtCell
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.entityHeader

fun FlowContent.eventShell(event: EventLocation) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            entityHeader(
                entity = event,
                descriptor = "at",
                cells = listOfNotNull(
                    startsAtCell(event.startsAt),
                    event.cost?.let { costCell(it, event.url) },
                    starCell(event.host),
                ),
                editRoute = EventUpdateRoute(event.eventSlug),
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
            appFooter(EventShell.SourcePath)
        }
    }

    dataIsland(EventShell.island, event)
}

object EventShell {
    val island = Id("event-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shells/eventShell.kt"
}