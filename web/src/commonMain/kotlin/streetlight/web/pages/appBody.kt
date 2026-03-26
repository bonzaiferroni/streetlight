package streetlight.web.pages

import koala.LottieFiles
import koala.SvgFiles
import koala.css.*
import koala.html.*
import koala.jsFileOf
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.AccountRoute
import streetlight.web.HomeRoute
import streetlight.web.SiteConfigRoute

fun HTML.appBody(
    block: (DIV.() -> Unit)? = null
) {
    body {
        box(AppBody.viewportId) {
            column(AppBody.appBoxId) {
                column {
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
        action(HomeRoute, modify(Flex1)) {
            row(modify(AlignItemsCenter)) {
                logo(modify(Height6))
//                image("/www/svg/flame.svg", modify(Height6, Glow))
                heading2("Streetlight")
                wireBlock(AppBody.titlePathId)
            }
        }
//        action(FullMapRoute, modify(Height6, Opacity2)) {
//            icon(SvgPath.chevronDown, modify(Height100))
//        }
        action(SiteConfigRoute, modify(Height6, OpacitySome)) {
            icon(SvgFiles.settings, modify(Height100))
        }
        action(AccountRoute, modify(Height6, Square), id = AppBody.badgeId) {
            emptyBadge()
        }
    }
}

fun FlowContent.appFooter() {
    column {
        configureAppFooter()
    }
}

fun DIV.configureAppFooter() {
    val giants = "May we build a world of faithful giants."
    setModifiers(modify(JustifyContentCenter, Height48, AlignItemsCenter, Gap0))
    lottie(LottieFiles.spinningCircles, modify(Height24))
    textBlock(giants, modify(Italic, OpacityMost))
}

fun FlowContent.emptyBadge() {
    icon(SvgFiles.emptyProfile, modify(Height100, OpacitySome))
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