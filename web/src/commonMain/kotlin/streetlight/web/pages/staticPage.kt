package streetlight.web.pages

import koala.JsFile
import koala.css.KoalaBody
import koala.css.KoalaJs
import koala.html.appHead
import koala.html.applyFiles
import koala.html.box
import koala.html.div
import koala.html.linkFira
import koala.html.scriptUnsafe
import kotlinx.html.FlowContent
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

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