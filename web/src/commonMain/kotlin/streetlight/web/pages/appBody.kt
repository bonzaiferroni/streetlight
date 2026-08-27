@file:Suppress("CssInvalidHtmlTagReference")

package streetlight.web.pages

import koala.JsBundle
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import streetlight.model.data.PageTheme

fun HTML.appBody(
    screen: AppScreen,
    theme: PageTheme? = null,
    block: DIV.() -> Unit = { }
) {
    body {
        theme?.let {
            applyTheme(theme)
        }
        setAttribute(KoalaBody.ScreenId.to(screen.screenId))
        div(AppBody.Viewport) {
            div(AppBody.PanelGrid) {
                box(AppBody.LeftPanel) {
                    siteMenuSidebar()
                }
                box(AppBody.ContentPanel) {
                    div(KoalaBody.PortalMount)
                    div(id = KoalaBody.ShellMount, block = block)
                }
                div(AppBody.RightPanel) {
                    div(StarHelm.StarMenu)
                }
            }
        }
        div(AppBody.FullScreen)
        appOverlay()
        div(AppBody.ToasterId)

        scriptUnsafe(AppOverlayJs)
        linkScript(JsBundle.Web)
    }
}

object AppBody {
    val Viewport = Id("app-viewport")
    val PanelGrid = Id("panel-grid")
    val ContentPanel = Id("content-panel")
    val LeftPanel = Id("left-panel")
    val RightPanel = Id("right-panel")
    val FullScreen = Id("full-screen")
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
    grid-template-columns: minmax(auto, 1fr) minmax(0, var(--content-panel-width)) minmax(auto, 1fr);
    /* grid-template-columns: auto minmax(0, var(--content-panel-width)) auto; */
    grid-template-areas: "left content right";
    justify-content: center;
}

$ContentPanel {
    grid-area: content;
}

$LeftPanel,
$RightPanel {
    display: none;
    position: sticky;
    place-items: center;
    top: calc(var(--unit-spacing) * 8);
    height: calc(100lvh - var(--unit-spacing) * 16);
    /* min-width: calc((100vw - ${CONTENT_PANEL_WIDTH_PX}px) / 2); */
    min-width: ${SIDE_PANEL_WIDTH_PX}px;
    
    @media (min-width: ${CONTENT_PANEL_WIDTH_PX + SIDE_PANEL_WIDTH_PX * 2}px) {
        display: grid;
    }
}

body${AppOverlay.RevealRightPanel} $RightPanel,
body${AppOverlay.RevealLeftPanel} $LeftPanel {
    display: grid;
}

$LeftPanel {
    grid-area: left;
}

$RightPanel {
    grid-area: right;
    /* padding-right: var(--unit-spacing-1); */
    /* width: var(--right-panel-width); */
}

$LeftPanel {
    /* padding-left: var(--unit-spacing-1); */
}

${KoalaBody.PortalMount},
${KoalaBody.ShellMount} {
    display: flex;
    flex-direction: column;
    grid-area: 1 / 1;
    min-width: 0;
}

${AppBody.FullScreen} {
    position: fixed;
    inset: 0;
    pointer-events: none;
    opacity: 0;
    transition: opacity var(--magic-interval) var(--magic-easing);
}

${AppBody.FullScreen}$Reveal {
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