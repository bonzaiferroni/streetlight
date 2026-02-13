package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.html.js.div

fun RenderContext.viewFullMap(app: AppContext) {
    val element = div { }
    val fullscreen = fullscreenBox {
        viewGeoMap(app.geoMap, modify(Height100))
    }
    element.onView { isVisible ->
        if (isVisible) {
            fullscreen.showModal()
        }
        console.log("onView: $isVisible")
    }

    fullscreen.addEventListener(OnClose) {
        app.portal.goBack()
    }
}

//    mountFullscreen(Id("fullscreen-map")) {
//        viewGeoMap(app.home.geoMap, modify(Height100))
//    }