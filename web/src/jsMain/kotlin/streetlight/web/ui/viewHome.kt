package streetlight.web.ui

import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.card
import koala.html.heading3
import koala.html.fillImage
import koala.html.textBlock
import koala.model.mapDistinct
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventStar
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShellKey
import streetlight.web.shells.HomeContent
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome() {
    val app = model
    val content = HomeContent(emptyList(), emptyList())
    val root = shellBox(HomeShellKey.ContainerId, app.geoMap, app.appScope) {
        homeShell(content)
    }

    queryAndWireSwitch(root, Id("bruh"), onToggle = { console.log("bruh") })
    wireStarSetters(app, root)
    wireStarredEvents(root)

    wireStreetMap()
}

fun ViewContext<Streetlight>.wireStarredEvents(root: HTMLElement) {
    val app = model
    val eventStarCache = app.userCache.eventStar
    val eventsFlow = eventStarCache.stateFlow.mapDistinct { it.events }
    val swapIdFlow = eventsFlow.mapDistinct {
        when (it.isEmpty()) {
            true -> HomeShellKey.StarInfoId
            else -> HomeShellKey.StarEventsId
        }
    }

    queryAndWireSwapBlock(root, HomeShellKey.StarSwapId, bindFlow = swapIdFlow)
    wireBlock(HomeShellKey.StarEventsId, root, wireOnView = false) {
        flowBlock(eventsFlow) { events ->
            box {
                val eventMap = events.groupBy { it.startsAt.toRelativeDayFormat() }
                row {
                    eventMap.forEach { (day, events) ->
                        column(modify(Gap0)) {
                            heading3(day, modify(LineHeight1, Margin1))
                            row(modify(Flex1)) {
                                events.forEach { event ->
                                    card(modify(Width24, BorderRadius1)) {
                                        fillImage(event.imageUrl, modify(Flex1))
                                        column(modify(Gap0)) {
                                            textBlock(event.title, modify(SingleLine, TextOverflowHidden))
                                            textBlock(event.locationName, modify(SingleLine, TextOverflowHidden, Dim))
                                        }
                                        row(modify(JustifyContentSpaceBetween)) {
                                            textBlock(event.startsAt.toTimeFormat())
                                            icon(SvgFile.Minus, modify(Dim)).onClick {
                                                eventStarCache.editStar(EventStar(event.eventId, null))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}