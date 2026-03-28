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
        stickyBar()
        box(AppBodyKey.ViewportId) {
            column(AppBodyKey.AppBoxId) {
                column {
                    appHeader()
                    box(AppBodyKey.ContentBox) {
                        box(AppBodyKey.PortalMountId)
                        box(id = AppBodyKey.ShellBoxId, block = block)
                    }
                }
            }
            box(OverlayId.mount)
        }
        applyJsFile(jsFileOf("streetlight/web.js"))
    }
}

fun FlowContent.appHeader() {
    val height = Height6
    filigree(modify(height, MarginTop1)) {
        action(HomeRoute, modify(DisplayFlex)) {
            row(modify(AlignItemsCenter)) {
                logo(modify(height))
                heading2("Streetlight", modify(GrowText, TextShadow))
//                    wireBlock(AppBody.titlePathId)
            }
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

fun FlowContent.stickyBar() {
    val cardMod = modify(BlurBackdrop, PointerEventsAuto)
    val iconMod = modify(Height6, AspectRatio1, DisplayFlex)
    row(AppBodyKey.StickyBar, modify(JustifyContentSpaceBetween)) {
        card(cardMod + LeftStickyCard) {
            action(SiteConfigRoute, iconMod + OpacityMost) {
                icon(SvgFile.Helm)
            }
        }
        card(cardMod + RightStickyCard) {
            action(AccountRoute, iconMod, id = AppBodyKey.BadgeId) {
                emptyBadge()
            }
        }
    }
}

object AppBodyKey {
    val ViewportId = Id("viewport-box")
    val AppBoxId = Id("app-box")
    val PortalMountId = Id("portal-mount")
    val ShellBoxId = Id("shell-box")
    val ContentBox = Id("content-box")
    val BadgeId = Id("user-badge")
    val StickyBar = Id("sticky-bar")
}

private val LeftStickyCard = Css("left-sticky-card")
private val RightStickyCard = Css("right-sticky-card")

// language="CSS"
val AppBodyCss = """
${AppBodyKey.StickyBar.selector} {
    position: fixed;
    pointer-events: none;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 14;
}

${RightStickyCard.selector}
${LeftStickyCard.selector} {
    background: rgba(var(--paper), .8);
}

${LeftStickyCard.selector} {
    border-radius: 0 20% 40% 20%;
}

${RightStickyCard.selector} {
    border-radius: 20% 0 20% 40%;
}
"""