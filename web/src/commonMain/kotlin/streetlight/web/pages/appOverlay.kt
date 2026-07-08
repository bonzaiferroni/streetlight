@file:Suppress("JSUnresolvedReference", "CssInvalidMediaFeature")

package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick

fun FlowContent.appOverlay() {
    column(AppOverlay.Container) {
        helmBar()
        spacer(modify(Flex1))

        // td: move panel toggles to panel containers for width-sensitive visibility
        // panel toggles
        row(modify(Height8, Padding1, JustifyContentSpaceBetween)) {
            icon(SvgFile.PanelLeft, modify(PointerEventsAuto, Dim, AppOverlay.MediaVlgReveal)) {
                onClick = AppOverlay.TogglePanel.invoke(AppBody.LeftPanel)
            }
            icon(SvgFile.PanelRight, modify(PointerEventsAuto, Dim, AppOverlay.MediaVlgReveal)) {
                onClick = AppOverlay.TogglePanel.invoke(AppBody.RightPanel)
            }
        }
    }
}

// language="JS"
val AppOverlayJs get() = """

${AppOverlay.TogglePanel} {
    const modifier = `${Reveal.identifier}`;
    const element = document.getElementById($panelArg);
    document.startViewTransition(() => {
        element.classList.toggle(modifier);
        const isToggled = element.classList.contains(modifier);
        localStorage.setItem($panelArg, isToggled ? 'true' : 'false');
    })
}

function initPanel(id) {
    if (localStorage.getItem(id) === 'true') {
        document.getElementById(id).classList.add(`${Reveal.identifier}`);
    }
}

initPanel(${AppBody.RightPanel.jsArg});
initPanel(${AppBody.LeftPanel.jsArg});

"""

private val panelArg = "panelId"

object AppOverlay {
    val Container = Id("app-overlay")
    val TogglePanel = JsFun("togglePanel", panelArg)
    val SpacerMiddleId = Id("spacer-middle")
    val MediaVlgReveal = Class("display-none-below-vlg")

    val VlgWidthPx = 1000
}

// language="CSS"
val AppOverlayCss get() = with(AppOverlay) { """
$Container {
    position: fixed;
    pointer-events: none;
    inset: 0;
}

$MediaVlgReveal {
    opacity: 1;
    transform: translate(0px, 0px);
    
    transition: 
        opacity var(--magic-interval),
        transform var(--magic-interval),
        visibility var(--magic-interval) allow-discrete;
}

@media (max-width: ${CONTENT_PANEL_WIDTH_PX + 100}px) {
    $MediaVlgReveal {
        transform: var(--slide-up-initial);
        opacity: 0;         
        visibility: hidden;    
    }
}

@starting-style {
    $MediaVlgReveal {
        opacity: 0;
        transform: var(--slide-up-initial);
    }
}

""" }