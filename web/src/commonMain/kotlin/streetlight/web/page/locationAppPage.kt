package streetlight.web.page

import koala.JsFile
import koala.PageResource
import koala.model.AppScreen
import koala.page.appHead
import koala.page.applyFiles
import koala.page.linkScript
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.model.data.PageTheme
import koala.component.*
import koala.modifier.*
import koala.page.*

/**
 * A page of a LocationApp for [screen]: the portal mount and the server-rendered shell of [shellBlock] in the
 * content panel, and the toaster, with none of the app's menus, overlay or footer. The page's [theme] is applied to
 * the body.
 */
fun HTML.locationAppPage(
    title: String,
    screen: AppScreen,
    resource: PageResource,
    theme: PageTheme? = null,
    shellBlock: (DIV.() -> Unit)? = null
) {
    appHead(title, resource) {
        supportGeoMap()
        applyFiles(JsFile)
        linkScript(resource.bundle.koala)
    }
    body {
        theme?.let {
            applyTheme(theme)
        }
        setAttribute(KoalaBody.ScreenId.to(screen.screenId))
        div(AppBody.Viewport) {
            div(AppBody.PanelGrid) {
                box(AppBody.ContentPanel) {
                    div(KoalaBody.PortalMount)
                    shellBlock?.let {
                        div(KoalaBody.ShellMount, block = it)
                    }
                }
            }
        }
        div(AppBody.ToasterId)

        linkScript(resource.bundle.locationApp)
    }
}
