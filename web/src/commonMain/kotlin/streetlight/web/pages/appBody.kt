package streetlight.web.pages

import koala.css.*
import koala.html.*
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
    row(modify(MinHeightAuto)) {
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
        action(AccountRoute, modify(Height6, Square), id = AppBody.badgeId) {
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