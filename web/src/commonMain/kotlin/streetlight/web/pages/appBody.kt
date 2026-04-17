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
    block: DIV.() -> Unit = { }
) {
    body {
        appOverlay()
        column(AppBodyKey.ViewportId, modify(Gap0)) {
            appHeader()
            row(AppBodyKey.ContentRowId, modify(JustifyContentCenter, Flex1)) {
                // div(AppBodyKey.SpacerLeftId)
                box(AppBodyKey.ContentBoxId) {
                    div(AppBodyKey.PortalMountId)
                    div(id = AppBodyKey.ShellBoxId, block = block)
                }
                div(AppBodyKey.SpacerRightId) {
                    column(modify(Height100P, Gap0)) {
                        spacer(modify(Height8))
                        column(AppBodyKey.PanelRightId, modify(Flex1, MarginRight1, AppOverlayKey.MediaVlgReveal))
                        spacer(modify(Height8))
                    }
                }
            }
        }

        scriptUnsafe(AppOverlayJs)
        linkScript(JsFile.Web)
    }
}

object AppBodyKey {
    val ViewportId = Id("viewport-box")
    val PortalMountId = Id("portal-mount")
    val ShellBoxId = Id("shell-box")
    val ContentBoxId = Id("content-box")
    val SpacerLeftId = Id("spacer-left")
    val SpacerRightId = Id("spacer-right")
    val PanelLeftId = Id("panel-left")
    val PanelRightId = Id("panel-right")
    val ContentRowId = Id("app-content-row")
}

// language="CSS"
val AppBodyCss get() = """
${AppBodyKey.ViewportId} {
    width: 100vw;
    height: 100dvh;
}

${AppBodyKey.ContentRowId} {
    height: calc(100dvh - var(--unit-spacing) * 8)
}

${AppBodyKey.ContentBoxId} {
    max-width: var(--body-width);
    width: 100%;
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

${AppBodyKey.ContentBoxId}$Reveal {
    opacity: 1;
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
    width: ${SIDE_PANEL_WIDTH_PX}px;
}

@media (min-width: 1000px) {
    ${AppBodyKey.SpacerLeftId}$Reveal,
    ${AppBodyKey.SpacerRightId}$Reveal {
        display: block;
    }
}

${AppBodyKey.SpacerLeftId} > *,
${AppBodyKey.SpacerRightId} > * {
    position: fixed;
    top: 0;
    width: inherit;
}

"""

const val RIGHT_PANEL_KEY = "streetlight.right-panel"