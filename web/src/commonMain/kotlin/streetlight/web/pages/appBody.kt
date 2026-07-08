package streetlight.web.pages

import koala.JsFile
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.web.HomeRoute

fun HTML.appBody(
    screen: AppScreen,
    block: DIV.() -> Unit = { }
) {
    body {
        setAttribute(KoalaBody.ScreenId.to(screen.screenId))
        div(AppBody.Viewport) {
            div(AppBody.PanelGrid) {
                box(AppBody.LeftPanel, modify(PlaceItemsCenter)) {
                    siteMenuSidebar()
                }
                box(AppBody.ContentPanel) {
                    div(KoalaBody.PortalMount)
                    div(id = KoalaBody.ShellMount, block = block)
                }
                div(AppBody.RightPanel)
            }
        }
        appOverlay()
        div(AppBody.FullScreenId)
        div(AppBody.ToasterId)

        scriptUnsafe(AppOverlayJs)
        linkScript(JsFile.Web)
    }
}

object AppBody {
    val Viewport = Id("app-viewport")
    val PanelGrid = Id("panel-grid")
    val ContentPanel = Id("content-panel")
    val LeftPanel = Id("left-panel")
    val RightPanel = Id("right-panel")
    val RightSticky = Id("right-sticky")
    val FullScreenId = Id("full-screen")
    val ToasterId = Id("toaster")
}

// language="CSS"
val AppBodyCss get() = with(AppBody) { """
$Viewport {
    width: 100vw;
    isolation: isolate;
}

$PanelGrid {
    display: grid;
    min-height: 100lvh;
    gap: var(--unit-spacing-1);
    grid-template-columns: auto minmax(0, var(--content-panel-width)) auto;
    grid-template-areas: "left content right";
    /* grid-template-columns: minmax(auto, 1fr) minmax(0, var(--content-panel-width)) minmax(auto, 1fr); */
    justify-content: center;
}

$ContentPanel {
    grid-area: content;
}

$LeftPanel,
$RightPanel {
    position: sticky;
    top: calc(var(--unit-spacing) * 8);
    height: calc(100lvh - var(--unit-spacing) * 16);
    /* min-width: calc((100vw - ${CONTENT_PANEL_WIDTH_PX}px) / 2); */
}

body:not(${AppOverlay.RevealRightPanel}) $RightPanel,
body:not(${AppOverlay.RevealLeftPanel}) $LeftPanel {
    display: none;
}

$LeftPanel {
    grid-area: left;
}

$RightPanel {
    grid-area: right;
    padding-right: var(--unit-spacing-1);
    width: var(--right-panel-width);
    
    @media (max-width: ${CONTENT_PANEL_WIDTH_PX - RIGHT_PANEL_WIDTH_PX}px) {
        display: none;
    }
}

$LeftPanel {
    padding-left: var(--unit-spacing-1);
}

${KoalaBody.PortalMount},
${KoalaBody.ShellMount} {
    width: 100%;
    grid-area: 1 / 1;
    min-width: 0;
}

${KoalaBody.PortalMount} > *,
${KoalaBody.ShellMount} > * {
    width: 100%;
}

${AppBody.FullScreenId} {
    position: fixed;
    inset: 0;
    pointer-events: none;
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

${AppBody.FullScreenId}$Reveal {
    pointer-events: auto;
    opacity: 1;
}

${AppBody.ToasterId} {
    position: fixed;
    inset: 0;
    pointer-events: none;
    display: flex;
    flex-direction: column;
    justify-content: end;
}

""" }

const val RIGHT_PANEL_KEY = "streetlight.right-panel"