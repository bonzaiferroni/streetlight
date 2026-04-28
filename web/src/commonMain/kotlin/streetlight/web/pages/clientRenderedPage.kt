package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.HTML

fun HTML.clientRenderedPage(styles: String) {
    appHead("Streetlight | Home", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        // homeShell(content)
    }
}