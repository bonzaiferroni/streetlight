package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.image
import kotlinx.browser.document
import kotlinx.html.js.div
import streetlight.web.pages.SinglePageId

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