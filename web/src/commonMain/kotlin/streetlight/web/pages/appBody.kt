package streetlight.web.pages

import koala.JsFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body

fun HTML.appBody(
    block: DIV.() -> Unit = { }
) {
    body {
        column(AppBodyKey.ViewportId, modify(Gap0)) {
            appHeader()
            row(AppBodyKey.ContentRowId, modify(JustifyContentCenter, Flex1, Gap0)) {
                // div(AppBodyKey.SpacerLeftId)
                box(AppBodyKey.ContentBoxId, modify(Flex3, PaddingX1)) {
                    div(AppBodyKey.PortalMountId)
                    div(id = AppBodyKey.ShellBoxId, block = block)
                }
                div(AppBodyKey.PanelRightId, modify(Flex1, AppOverlayKey.MediaVlgReveal))
            }
        }
        appOverlay()
        div(AppBodyKey.FullScreenId)
        div(AppBodyKey.ToasterId)

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
    val FullScreenId = Id("full-screen")
    val ToasterId = Id("toaster")
}

// language="CSS"
val AppBodyCss get() = """
${AppBodyKey.ViewportId} {
    width: 100vw;
    isolation: isolate;
}

${AppBodyKey.ContentRowId} {
    height: calc(100dvh - var(--unit-spacing) * 8)
}

${AppBodyKey.ContentBoxId} {
    max-width: var(--body-width);
    width: 100%;
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
    
    ${AppBodyKey.PanelRightId}$Reveal {
        display: block;
    }
}

${AppBodyKey.SpacerLeftId} > *,
${AppBodyKey.SpacerRightId} > * {
    position: fixed;
    top: 0;
    width: inherit;
}

${AppBodyKey.PanelRightId} {
    position: sticky;
    top: calc(var(--unit-spacing) * 8);
    height: calc(100dvh - var(--unit-spacing) * 16);
    display: none;
    padding-right: var(--unit-spacing-1);
    max-width: 500px;
}

${AppBodyKey.FullScreenId} {
    position: fixed;
    inset: 0;
    pointer-events: none;
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

${AppBodyKey.FullScreenId}$Reveal {
    pointer-events: auto;
    opacity: 1;
}

${AppBodyKey.ToasterId} {
    position: fixed;
    inset: 0;
    pointer-events: none;
    display: flex;
    flex-direction: column;
    justify-content: end;
}

"""

const val RIGHT_PANEL_KEY = "streetlight.right-panel"