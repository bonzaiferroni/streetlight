package streetlight.web.pages

import koala.LottieFile
import koala.SvgFile
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
    val iconMod = modify(AspectRatio1, DisplayFlex)
    val height = Height6
    row(modify(height)) {
        action(SiteConfigRoute, iconMod + OpacityHalf) {
            icon(SvgFile.Helm)
        }
        row(modify(Flex1, JustifyContentCenter, MarginX1)) {
            action(HomeRoute, modify(DisplayFlex)) {
                row(modify(AlignItemsCenter)) {
                    logo(modify(height))
                    heading2("Streetlight", modify(GrowText, TextShadow))
//                    wireBlock(AppBody.titlePathId)
                }
            }
        }
        action(AccountRoute, iconMod, id = AppBody.badgeId) {
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
    addModifiers(modify(JustifyContentCenter, Height48, AlignItemsCenter, Gap0))
    lottie(LottieFile.spinningCircles, modify(Height24))
    textBlock(giants, modify(Italic, OpacityMost))
}

fun FlowContent.emptyBadge() {
    icon(SvgFile.EmptyProfile, modify(OpacityHalf, Size100P))
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