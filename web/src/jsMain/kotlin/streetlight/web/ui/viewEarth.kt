package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading3
import streetlight.web.model.Earth
import streetlight.web.pages.AppBody
import web.dom.document

fun ViewScope.viewEarth(model: Earth) {
    box(EarthStyle.Container, Size100P) {
        val cameraController = geoMapMount(mod = EarthStyle.Map)
        column(modify(Gap0, PointerEventsNone)) {
            div(modify(EarthStyle.Grid, Flex1, MinHeight(0))) {
                earthUnboundedOverlay(model, cameraController)
                earthHeader(model)
                earthMenu(model)
                earthFocus(model)
            }.flowModifier(model.isFocusedState, EarthStyle.IsFocused, contentScope)
        }
    }.flowModifier(model.isMovingState, EarthStyle.IsMoving, contentScope)
}

fun ViewScope.viewEarthRoute() {
    val element = document.getElementById(AppBody.FullScreen)
    element.modify(Reveal)

    this@viewEarthRoute.mountChildView("earth", element) {
        val model = app.getEarthMap(contentScope, null)
        viewEarth(model)
    }

    onDispose {
        element.unmodify(Reveal)
        element.clearAfterInterval()
    }
}

fun ViewScope.earthHeader(model: Earth) {
    flowBlock(model.mapState, modify(EarthStyle.Header, EarthStyle.MoveDimmer, Magic)) { map ->
        if (map == null) return@flowBlock
        column(modify(Height(8), EarthStyle.MapTitle, JustifyContentCenter)) {
            filigree {
                heading3(map.title)
            }
            // button("Show All", modify(Zen, PointerEventsAuto, BlurBackdrop)).onClick(model::showAll)
        }
    }
}
