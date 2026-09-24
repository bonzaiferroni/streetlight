package streetlight.web.pages

import koala.PageResource
import koala.modifier.KoalaBody
import koala.html.appHead
import koala.html.box
import koala.html.div
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body

/** A page without the client app, showing [block] beside the site menu. */
fun HTML.staticPage(
    title: String,
    resource: PageResource,
    block: FlowContent.() -> Unit
) {
    appHead(title, resource) {

    }
    body {
        div(AppBody.Viewport) {
            div(AppBody.PanelGrid) {
                box(AppBody.LeftPanel) {
                    siteMenuSidebar()
                }
                box(AppBody.ContentPanel) {
                    div(id = KoalaBody.ShellMount, block = block)
                }
                div(AppBody.RightPanel) {
                    // div(StarHelm.BarMenu)
                }
            }
        }
    }
}