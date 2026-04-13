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
        appOverlay()
        div(AppBodyKey.ViewportId) {
            div(AppBodyKey.SpacerLeftId)
            column(AppBodyKey.AppPanelId, modify(Flex1)) {
                appHeader()
                box(AppBodyKey.ContentBoxId) {
                    box(AppBodyKey.PortalMountId)
                    box(id = AppBodyKey.ShellBoxId, block = block)
                }
            }
            div(AppBodyKey.SpacerRightId) {
                column(modify(Height100P, Gap0)) {
                    spacer(modify(Height8))
                    column(AppBodyKey.PanelRightId, modify(Flex1, MarginRight1))
                    spacer(modify(Height8))
                }
            }
        }

        scriptUnsafe(AppOverlayJs)
        linkScript(JsFile.Web)
    }
}

object AppBodyKey {
    val ViewportId = Id("viewport-box")
    val AppPanelId = Id("app-box")
    val PortalMountId = Id("portal-mount")
    val ShellBoxId = Id("shell-box")
    val ContentBoxId = Id("content-box")
    val SpacerLeftId = Id("spacer-left")
    val SpacerRightId = Id("spacer-right")
    val PanelLeftId = Id("panel-left")
    val PanelRightId = Id("panel-right")
}

// language="CSS"
val AppBodyCss get() = """
${AppBodyKey.ViewportId} {
    width: 100vw;
    height: 100dvh;
    display: flex;
    justify-content: center;
}

${AppBodyKey.AppPanelId} {
    min-height: 100dvh;
    max-width: var(--body-width);
    padding: var(--unit-spacing);
}

${AppBodyKey.AppPanelId} {
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

${AppBodyKey.SpacerLeftId},
${AppBodyKey.SpacerRightId} {
    display: none;
}

${AppBodyKey.SpacerLeftId}$Reveal,
${AppBodyKey.SpacerRightId}$Reveal {
    display: block;
    width: ${SIDE_PANEL_WIDTH_PX}px;
}

${AppBodyKey.SpacerLeftId} > *,
${AppBodyKey.SpacerRightId} > * {
    position: fixed;
    top: 0;
    width: inherit;
}

/* content box */
${AppBodyKey.ContentBoxId} {
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

${AppBodyKey.ContentBoxId}$Reveal {
    opacity: 1;
}
"""

const val RIGHT_PANEL_KEY = "streetlight.right-panel"