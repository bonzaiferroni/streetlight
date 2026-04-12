package streetlight.web.pages

import koala.JsFile
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.HomeRoute

fun HTML.appBody(
    block: (DIV.() -> Unit)? = null
) {
    body {
        column(AppBodyKey.AppOverlayId) {
            helmBar()
            spacer(modify(Flex1))
            row(modify(Height6, Padding1)) {
                spacer(modify(Flex1))
                icon(SvgFile.PanelRight)
            }
        }
        box(AppBodyKey.ViewportId) {
            column(AppBodyKey.LeftPanelId, modify(Flex1))
            column(AppBodyKey.AppPanelId) {
                appHeader()
                box(AppBodyKey.ContentBoxId) {
                    box(AppBodyKey.PortalMountId)
                    box(id = AppBodyKey.ShellBoxId, block = block)
                }
            }
            column(AppBodyKey.RightPanelId, modify(Flex1))
        }
        applyJsFile(JsFile.Web)
    }
}

fun FlowContent.appHeader() {
    val height = Height6
    row(modify(height, JustifyContentCenter)) {
        icon(SvgFile.Rays, modify(OpacitySome, IconKey.Stretch, Width16))
        action(HomeRoute, modify(DisplayFlex)) {
            row(modify(AlignItemsCenter)) {
                logo(modify(height))
                heading2("Streetlight", modify(GrowText, TextShadow))
            }
        }
        icon(SvgFile.Rays, modify(OpacitySome, IconKey.Stretch, Width16, FlipX))
    }
}

object AppBodyKey {
    val ViewportId = Id("viewport-box")
    val AppPanelId = Id("app-box")
    val PortalMountId = Id("portal-mount")
    val ShellBoxId = Id("shell-box")
    val ContentBoxId = Id("content-box")
    val RightPanelId = Id("right-panel")
    val LeftPanelId = Id("left-panel")
    val AppOverlayId = Id("app-overlay")
}

// language="CSS"
val AppBodyCss = """
${AppBodyKey.ViewportId} {
    width: 100vw;
    height: 100dvh;
    display: flex;
    justify-content: center;
}

${AppBodyKey.AppPanelId} {
    min-height: 100vh;
    max-width: var(--body-width);
    padding: var(--unit-spacing);
}

${AppBodyKey.AppPanelId} {
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

${AppBodyKey.AppOverlayId} {
    position: fixed;
    pointer-events: none;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 14;
}

/* content box */
#content-box {
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

#content-box.reveal {
    opacity: 1;
}
"""