package streetlight.web.pages

import koala.JsFile
import koala.css.*
import koala.html.*
import koala.jsFileOf
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
        applyJsFile(jsFileOf("streetlight/web.js"))
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

fun FlowContent.appFooter() {
    val giants = "May we build a world of faithful giants."
    row(modify(JustifyCenter, Height48)) {
        column(modify(AlignItemsCenter, Gap0, Width100)) {
            lottie("spinning_circles", modify(Height24))
            textBlock(giants, modify(Italic, Opacity6))
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