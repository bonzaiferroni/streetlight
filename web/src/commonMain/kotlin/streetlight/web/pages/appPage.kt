package streetlight.web.pages

import koala.JsFile
import koala.PageResource
import koala.html.AppScreen
import koala.html.appHead
import koala.html.applyFiles
import koala.html.scriptUnsafe
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import streetlight.model.data.PageTheme
import streetlight.web.layouts.FeedModeScript

fun HTML.appPage(
    title: String,
    screen: AppScreen,
    resource: PageResource,
    theme: PageTheme? = null,
    block: (FlowContent.() -> Unit)? = null
) {
    appHead("$title | Streetlight", resource) {
        supportProtobuf()
        supportGeoMap()
        applyFiles(JsFile)
        scriptUnsafe(FeedModeScript)
    }
    appBody(screen, resource, theme, block)
}