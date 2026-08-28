package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.dedup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.web.io.OmniClient
import streetlight.web.pages.AppBody
import web.html.HTMLElement
import web.scroll.ScrollBehavior
import web.scroll.ScrollToOptions
import web.scroll.smooth
import kotlin.time.Duration.Companion.milliseconds

fun ViewScope.wireRightPanel() {
    val omni = app.get<OmniClient>()
    var container: HTMLElement? = null
    val cardMod = modify(ZenBg, Height100P, JustifyContentEnd, OverflowYAuto, OverscrollBehaviorContain, OverflowXHidden)

    wireBlock(AppBody.RightPanel) {
        container = card(cardMod) {
//            itemsBlock(recordFlow, modify(Magic, SlideLeft)) { record ->
//                textBlock(record.text, modify(TextSmall))
//            }
        }
    }

    contentScope.launch {
        omni.recordsState.flow.collect {
            delay(100.milliseconds)
            container?.scrollTo(ScrollToOptions(
                top = container.scrollHeight.toDouble(),
                behavior = ScrollBehavior.smooth
            ))
        }
    }
}