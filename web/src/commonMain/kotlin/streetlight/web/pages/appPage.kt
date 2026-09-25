package streetlight.web.pages

import koala.JsFile
import koala.PageResource
import koala.html.AppScreen
import koala.html.appHead
import koala.html.applyFiles
import koala.html.linkScript
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import streetlight.model.data.PageTheme
import koala.interop.HeadScriptConfig
import koala.interop.RootSwitch
import streetlight.web.layouts.FeedMode
import streetlight.web.layouts.FeedRow

/** An app page for [screen], with the head that supports the map, and the body of [appBody]. */
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
        linkScript(resource.bundle.koala)
    }
    appBody(screen, resource, theme, block)
}


