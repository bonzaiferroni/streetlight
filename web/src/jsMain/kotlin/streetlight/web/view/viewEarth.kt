package streetlight.web.view

import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.component.filigree
import koala.component.heading3
import streetlight.web.model.Earth
import streetlight.web.page.AppBody
import web.dom.document
import koala.dom.component.*
import koala.dom.layout.*
import koala.dom.page.*
import streetlight.web.utility.element
import streetlight.web.model.getEarthMap
import streetlight.web.component.workSignal
import streetlight.web.dom.component.workSignal

context(model: Earth)
fun ViewScope.viewEarth() {
    box(EarthStyle.Container, modify(Size100P, PositionRelative)) {
        geoMapMount(model.markerMap, mod = EarthStyle.Map)
        column(modify(Gap0, PointerEventsNone)) {
            div(modify(EarthStyle.Grid, Flex1, MinHeight(0))) {
                earthHeader()
                earthMenu()
                earthFocus()
            }.flowModifier(model.isFocusedState, EarthStyle.IsFocused, contentScope)
        }
        workSignal(model.isQueryingState, modify(Bottom0, Right0))
    }.flowModifier(model.isMovingState, EarthStyle.IsMoving, contentScope)
}

fun ViewScope.viewEarthRoute() {
    val element = document.getElementById(AppBody.FullScreen)
    element.modify(Reveal)

    this@viewEarthRoute.mountChildView("earth", element) {
        val model = app.getEarthMap(contentScope, null)
        context(model) {
            viewEarth()
        }
    }

    onDispose {
        element.unmodify(Reveal)
        element.clearAfterInterval()
    }
}

context(model: Earth)
fun ViewScope.earthHeader() {
    flowBlock(model.mapState, modify(EarthStyle.Header, Magic)) { map ->
        if (map == null) return@flowBlock
        column(Gap0) {
            column(modify(Height(8), EarthStyle.MapTitle, EarthStyle.MoveDimmer, JustifyContentCenter)) {
                filigree {
                    heading3(map.title)
                }
            }
            earthControls()
        }
    }
}
