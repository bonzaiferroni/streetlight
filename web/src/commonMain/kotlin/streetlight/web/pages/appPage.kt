package streetlight.web.pages

import koala.JsFile
import koala.html.AppScreen
import koala.html.appHead
import koala.html.applyFiles
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
        applyFiles(JsFile)
    }
    appBody(screen) {
        block()
    }
}