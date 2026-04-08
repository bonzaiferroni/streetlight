package streetlight.web.ui

import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import kampfire.model.medium
import koala.SvgFile
import koala.css.BorderRadius1
import koala.css.Dim
import koala.css.Flex1
import koala.css.FlexShrink0
import koala.css.Gap0
import koala.css.JustifyContentSpaceBetween
import koala.css.LineHeight1
import koala.css.Margin1
import koala.css.OverflowXAuto
import koala.css.WhiteSpaceNoWrap
import koala.css.TextOverflowHidden
import koala.css.Width24
import koala.css.modify
import koala.dom.ViewContext
import koala.dom.box
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.icon
import koala.dom.onClick
import koala.dom.queryAndWireSwapBlock
import koala.dom.row
import koala.dom.wireBlock
import koala.html.card
import koala.html.fillImage
import koala.html.heading3
import koala.html.textBlock
import koala.model.mapDistinct
import org.w3c.dom.HTMLElement
import streetlight.web.model.Streetlight
import streetlight.web.shells.HomeShellKey
import kotlin.collections.component1
import kotlin.collections.component2

fun ViewContext<Streetlight>.wireLitEvents(root: HTMLElement) {
    val app = model
    val eventCache = app.cache.event
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
            box {
                val eventMap = events.groupBy { it.startsAt.toRelativeDayFormat() }
                row(modify(OverflowXAuto)) {
                    eventMap.forEach { (day, events) ->
                        column(modify(Gap0, FlexShrink0)) {
                            heading3(day, modify(LineHeight1, Margin1))
                            row(modify(Flex1)) {
                                events.forEach { event ->
                                    card(modify(Width24, BorderRadius1)) {
                                        fillImage(event.images.medium, modify(Flex1))
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
                        }
                    }
                }
            }
        }
    }
}