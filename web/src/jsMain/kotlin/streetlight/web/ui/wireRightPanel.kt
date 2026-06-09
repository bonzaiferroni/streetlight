package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions
import streetlight.web.io.OmniLog
import streetlight.web.pages.AppBodyKey

fun RenderContext.wireRightPanel() {
    val omni = app.get<OmniLog>()
    val recordFlow = omni.stateFlow.mapDistinct { it.records }
    var container: HTMLElement? = null
    val cardMod = modify(ZenBg, Height100P, JustifyContentEnd, OverflowYAuto, OverscrollBehaviorContain, OverflowXHidden)

    wireBlock(AppBodyKey.PanelRightId) {
        container = card(cardMod) {
            itemsBlock(recordFlow, modify(Magic, SlideLeft)) { record ->
                textBlock(record.text, modify(SmallText))
            }
        }
    }

    renderScope.launch {
        recordFlow.collect {
            delay(100)
            container?.scrollTo(ScrollToOptions(
                top = container.scrollHeight.toDouble(),
                behavior = ScrollBehavior.SMOOTH
            ))
        }
    }
}