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
                    box(AppBodyKey.ContentBoxId) {
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
    filigree(modify(height)) {
        action(HomeRoute, modify(DisplayFlex)) {
            row(modify(AlignItemsCenter)) {
                logo(modify(height))
                heading2("Streetlight", modify(GrowText, TextShadow))
//                    wireBlock(AppBody.titlePathId)
            }
        }
    }
}

fun FlowContent.emptyBadge() {
    icon(SvgFile.EmptyProfile, modify(OpacityHalf, Size100P))
}

fun FlowContent.stickyBar() {
    val cardMod = modify(BlurBackdrop, PointerEventsAuto, BorderRadius50P, BlurBg, StickyCard)
    val iconMod = modify(Height6, AspectRatio1, DisplayFlex)
    row(AppBodyKey.StickyBarId, modify(JustifyContentSpaceBetween)) {
        card(cardMod) {
            action(SiteConfigRoute, iconMod + OpacityMost) {
                icon(SvgFile.Helm)
            }
        }
        card(cardMod) {
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
    val ContentBoxId = Id("content-box")
    val BadgeId = Id("user-badge")
    val StickyBarId = Id("sticky-bar")
}

private val StickyCard = Class("sticky-card")

// language="CSS"
val AppBodyCss = """
${AppBodyKey.ViewportId} {
    position: relative;
    width: 100vw;
    height: 100dvh;
}

${AppBodyKey.AppBoxId} {
    position: relative;
    min-height: 100vh;
    width: 100%;
    max-width: var(--body-width);
    margin: 0 auto;
    padding: var(--unit-spacing);
}

${AppBodyKey.AppBoxId} {
    width: 100%;
    display: grid;
}

${AppBodyKey.PortalMountId},
${AppBodyKey.ShellBoxId} {
    width: 100%;
    grid-area: 1 / 1;
    min-width: 0;
}

${AppBodyKey.PortalMountId} > *,
${AppBodyKey.ShellBoxId} > * {
    width: 100%;
}
    
${AppBodyKey.StickyBarId} {
    position: fixed;
    pointer-events: none;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 14;
}

$StickyCard {
    transition: background-color var(--magic-interval) var(--magic-easing);
}

@media (min-width: ${BODY_WIDTH_PX + 128}px) {
    $StickyCard {
        background-color: transparent;
    }
}
"""