package streetlight.web.pages

import koala.html.AppScreen
import koala.html.appHead
import kotlinx.html.FlowContent
import kotlinx.html.HTML

fun HTML.appPage(
    title: String,
    styles: String,
    screen: AppScreen,
    block: FlowContent.() -> Unit
) {
    appHead(title, styles) {
        supportGeoMap()
    }
    appBody(screen) {
        block()
    }
}