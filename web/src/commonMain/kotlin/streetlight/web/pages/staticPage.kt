package streetlight.web.pages

import koala.PageResource
import koala.modifier.KoalaBody
import koala.page.appHead
import koala.layout.box
import koala.layout.div
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