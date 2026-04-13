package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import streetlight.web.model.Streetlight
import streetlight.web.pages.AppBodyKey

fun RenderContext.wireRightPanel(app: Streetlight) {
    val omni = app.omni
    val recordFlow = omni.stateFlow.mapDistinct { it.records }

    wireBlock(AppBodyKey.PanelRightId) {
        card(modify(ZenCardBg, MoonShadow, Height100P, JustifyContentEnd)) {
            itemsBlock(recordFlow) { record ->
                textBlock(record.text)
            }
        }
    }
}