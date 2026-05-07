package streetlight.web.pages

import koala.html.appHead
import kotlinx.html.FlowContent
import kotlinx.html.HTML

fun HTML.appPage(title: String, styles: String, block: FlowContent.() -> Unit) {
    appHead(title, styles) {
        supportGeoMap()
    }
    appBody {
        block()
    }
}