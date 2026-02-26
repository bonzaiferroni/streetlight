package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import kotlinx.html.js.div

fun RenderContext.viewFullMap(app: AppContext) {
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

    fullscreen.addEventListener(OnClose) {
        app.portal.goBack()
    }

    wireGeoMap(app.geoMap, app.appScope, fullscreen)
}

//    mountFullscreen(Id("fullscreen-map")) {
//        viewGeoMap(app.home.geoMap, modify(Height100))
//    }