package streetlight.web.ui

import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.model.small
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.fillImage
import koala.html.heading3
import koala.model.mapDistinct
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.web.model.LightCache
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShellKey
import kotlin.collections.component1
import kotlin.collections.component2

fun ViewContext<Streetlight>.wireLitEvents(root: HTMLElement) {
    val app = model
    val eventCache = app.cache.eventLights
    val eventsFlow = eventCache.stateFlow.mapDistinct { it.items }
    val swapIdFlow = eventsFlow.mapDistinct {
        when (it.isEmpty()) {
            true -> HomeShellKey.LightInfoId
            else -> HomeShellKey.LitEventsId
        }
    }

    queryAndWireSwapBlock(root, HomeShellKey.LightSwapId, bindFlow = swapIdFlow)
    wireBlock(HomeShellKey.LitEventsId, root, wireOnView = false) {
        flowBlock(eventsFlow) { events ->
            val eventMap = events.groupBy { it.startsAt.toRelativeDayFormat() }
            row(modify(OverflowXAuto, Height100P)) {
                eventMap.forEach { (day, events) ->
                    column(modify(Gap0, FlexShrink0)) {
                        heading3(day, modify(LineHeight1, Margin1))
                        row(modify(Flex1)) {
                            events.forEach { event ->
                                narrowEventCard(event, eventCache)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RenderContext.narrowEventCard(event: EventLocation, eventCache: LightCache<EventId, EventLocation>) {
    card(modify(Width24, BorderRadius1)) {
        fillImage(event.images.small, modify(Flex1), fillWidth = false)
        column() {
            column(modify(Gap0)) {
                textBlock(event.title, modify(WhiteSpaceNoWrap, TextOverflowHidden))
                textBlock(event.locationName, modify(WhiteSpaceNoWrap, TextOverflowHidden, Dim))
            }
            row(modify(JustifyContentSpaceBetween)) {
                textBlock(event.startsAt.toTimeFormat())
                icon(SvgFile.Minus, modify(Dim)).onClick {
                    eventCache.removeLight(event.eventId)
                }
            }
        }
    }
}