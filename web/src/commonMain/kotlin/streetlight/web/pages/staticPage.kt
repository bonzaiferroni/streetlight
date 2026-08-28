package streetlight.web.pages

import koala.css.KoalaBody
import koala.html.appHead
import koala.html.box
import koala.html.div
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body

fun HTML.staticPage(
    title: String,
    styles: String,
    block: FlowContent.() -> Unit
) {
    appHead(title, styles) {

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