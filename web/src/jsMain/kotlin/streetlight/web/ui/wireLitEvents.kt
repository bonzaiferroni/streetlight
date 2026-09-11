package streetlight.web.ui

import kabinet.utils.toFutureFormat
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.fillImageSrcSet
import koala.html.heading3
import koala.model.dedup
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.web.model.DataCache
import streetlight.web.model.StarCache
import streetlight.web.shells.HomeShell
import web.html.HTMLElement
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Clock

fun ViewScope.wireLitEvents(root: HTMLElement) {
    val cache = app.get<DataCache>()

    val now = Clock.System.now()
    val eventCache = cache.eventStars
    val eventsFlow = eventCache.stateFlow.dedup { events -> events.items.filter {
        true
        // td: fix (it.endsAt ?: it.startsAt != null && it.startsAt + 4.hours) > now
    } }
    val swapIdFlow = eventsFlow.dedup {
        when (it.isEmpty()) {
            true -> HomeShell.LightInfoId
            else -> HomeShell.LitEventsId
        }
    }

    queryAndWireSwapBlock(root, HomeShell.LightSwapId, bindFlow = swapIdFlow)
    wireBlock(HomeShell.LitEventsId, root, wireOnView = false) {
        // td: fix later or delete
        flowBlock(emptyList(), eventsFlow) { events ->
            val eventMap = events.groupBy { it.startsAt?.toFutureFormat() }
            row(modify(OverflowXAuto, Height100Pct, Padding1)) {
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

fun ViewScope.narrowEventCard(event: EventLocation, eventCache: StarCache<EventId, EventLocation>) {
    card(modify(Width24, BorderRadius1, MoonShadow)) {
        fillImageSrcSet(event.image, modify(Flex1), fillWidth = false)
        column() {
            column(modify(Gap0)) {
                textBlock(event.title, modify(WhiteSpaceNoWrap, TextOverflowEllipses))
                textBlock(event.locationLabel, modify(WhiteSpaceNoWrap, TextOverflowEllipses, Dim))
            }
            row(modify(JustifyContentSpaceBetween)) {
                event.startsAt?.let {
                    textBlock(it.toTimeFormat())
                }
                icon(SvgFile.Minus, modify(Dim)).onClick {
                    eventCache.removeLight(event.eventId)
                }
            }
        }
    }
}