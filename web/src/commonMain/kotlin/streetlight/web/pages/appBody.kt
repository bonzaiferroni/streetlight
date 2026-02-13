package streetlight.web.pages

import koala.css.AlignItemsCenter
import koala.css.Height100
import koala.css.Height6
import koala.css.Opacity2
import koala.css.SpaceBetween
import koala.css.Width100
import koala.css.modify
import koala.html.Id
import koala.html.OverlayId
import koala.html.action
import koala.html.box
import koala.html.column
import koala.html.heading1
import koala.html.icon
import koala.html.logo
import koala.html.row
import koala.html.applyScripts
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.AccountRoute
import streetlight.web.FullMapRoute
import streetlight.web.HomeRoute

fun HTML.appBody(
    block: (DIV.() -> Unit)? = null
) {
    body {
        box(AppBody.viewportId) {
            column(AppBody.appBoxId) {
                column(modify(Width100, AlignItemsCenter)) {
                    row(modify(Width100, SpaceBetween)) {
                        action(FullMapRoute, modify(Height6, Opacity2)) {
                            icon("chevron-down", modify(Height100))
                        }
                        action(HomeRoute()) {
                            row {
                                logo()
                                heading1("Streetlight")
                            }
                        }
                        action(AccountRoute, modify(Height6, Opacity2)) {
                            icon("empty-profile", modify(Height100))
                        }
                    }
                    box(AppBody.contentBox) {
                        box(AppBody.portalMountId)
                        box(id = AppBody.shellBoxId, block = block)
                    }
                }
            }
            box(OverlayId.mount)
        }
        applyScripts("streetlight/web.js")
    }
}

object AppBody {
    val viewportId = Id("viewport-box")
    val appBoxId = Id("app-box")
    val portalMountId = Id("portal-mount")
    val shellBoxId = Id("shell-box")
    val contentBox = Id("content-box")
}