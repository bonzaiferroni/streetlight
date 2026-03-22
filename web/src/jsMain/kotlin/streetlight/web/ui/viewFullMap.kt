package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.ElementEvent
import koala.html.geoMapMount
import kotlinx.html.js.div
import streetlight.web.model.Streetlight

fun RenderContext.viewFullMap(app: Streetlight) {
    val element = div {

    }
    val fullscreen = fullscreenBox {
        geoMapMount(null, modify(Size100))
    }
    element.onView { isVisible ->
        if (isVisible) {
            fullscreen.showModal()
        }
        console.log("onView: $isVisible")
    }

    fullscreen.onEvent(ElementEvent.onClose) {
        app.portal.goBack()
    }

    wireGeoMap(app.geoMap, app.appScope, fullscreen)
}

//    mountFullscreen(Id("fullscreen-map")) {
//        viewGeoMap(app.home.geoMap, modify(Height100))
//    }