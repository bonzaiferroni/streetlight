package streetlight.web.pages

import koala.JsFile
import koala.html.AppScreen
import koala.html.appHead
import koala.html.applyFiles
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import streetlight.model.data.PageTheme

fun HTML.appPage(
    title: String,
    styles: String,
    screen: AppScreen,
    theme: PageTheme? = null,
    block: FlowContent.() -> Unit
) {
    appHead("$title | Streetlight", styles) {
        supportProtobuf()
        supportGeoMap()
        applyFiles(JsFile)
    }
    appBody(screen, theme) {
        block()
    }
}