package streetlight.web.pages

import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Glow
import koala.css.GlowBackground
import koala.css.GlowShadow
import koala.css.Height100
import koala.css.Height6
import koala.css.Opacity2
import koala.css.Square
import koala.css.Width100
import koala.css.modify
import koala.html.Id
import koala.html.OverlayId
import koala.html.action
import koala.html.applyId
import koala.html.box
import koala.html.column
import koala.html.icon
import koala.html.row
import koala.html.applyScripts
import koala.html.heading2
import koala.html.logo
import koala.html.wireBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.AccountRoute
import streetlight.web.FullMapRoute
import streetlight.web.HomeRoute
import streetlight.web.SiteConfigRoute
import streetlight.web.ui.SvgPath

fun HTML.appBody(
    block: (DIV.() -> Unit)? = null
) {
    body {
        box(AppBody.viewportId) {
            column(AppBody.appBoxId) {
                column(modify(Width100, AlignItemsCenter)) {
                    appHeader()
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

fun FlowContent.appHeader() {
    row(AppBody.appHeaderId, modify(Width100)) {
        action(HomeRoute(), modify(Flex1)) {
            row {
                logo(modify(Height6))
//                image("/www/svg/flame.svg", modify(Height6, Glow))
                heading2("Streetlight")
                wireBlock(AppBody.titlePathId)
            }
        }
//        action(FullMapRoute, modify(Height6, Opacity2)) {
//            icon(SvgPath.chevronDown, modify(Height100))
//        }
        action(SiteConfigRoute, modify(Height6, Opacity2)) {
            icon(SvgPath.settings, modify(Height100))
        }
        action(AccountRoute, modify(Height6, Square), AppBody.badgeId) {
            emptyBadge()
        }
    }
}

fun FlowContent.emptyBadge() {
    icon(SvgPath.emptyProfile, modify(Height100, Opacity2))
}

object AppBody {
    val viewportId = Id("viewport-box")
    val appBoxId = Id("app-box")
    val portalMountId = Id("portal-mount")
    val shellBoxId = Id("shell-box")
    val contentBox = Id("content-box")
    val appHeaderId = Id("app-header")
    val titlePathId = Id("title-path")
    val badgeId = Id("user-badge")
}