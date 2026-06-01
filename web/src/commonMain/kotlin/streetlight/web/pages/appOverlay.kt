@file:Suppress("JSUnresolvedReference")

package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick

fun FlowContent.appOverlay() {
    column(AppOverlayKey.AppOverlayId) {
        helmBar()
        spacer(modify(Flex1))
        row(modify(Height8, Padding1)) {
            spacer(modify(Flex1))
            icon(SvgFile.PanelRight, modify(PointerEventsAuto, Dim, AppOverlayKey.MediaVlgReveal)) {
                onClick = AppOverlayKey.TogglePanel.invoke(AppBodyKey.PanelRightId)
            }
        }
    }
}

// language="JS"
val AppOverlayJs get() = """

${AppOverlayKey.TogglePanel} {
    const modifier = `${Reveal.identifier}`;
    const element = document.getElementById($panelArg);
    document.startViewTransition(() => {
        element.classList.toggle(modifier);
        const isToggled = element.classList.contains(modifier);
        localStorage.setItem('$RIGHT_PANEL_KEY', isToggled ? 'true' : 'false');
    })
}

function initRightPanel() {
    if (localStorage.getItem('$RIGHT_PANEL_KEY') === 'true') {
        document.getElementById(${AppBodyKey.PanelRightId.arg}).classList.add(`${Reveal.identifier}`);
    }
}

initRightPanel();

"""

private val panelArg = "panelId"

object AppOverlayKey {
    val AppOverlayId = Id("app-overlay")
    val TogglePanel = JsFun("togglePanel", panelArg)
    val SpacerMiddleId = Id("spacer-middle")
    val MediaVlgReveal = Class("display-none-below-vlg")
}

// language="CSS"
val AppOverlayCss get() = """
${AppOverlayKey.AppOverlayId} {
    position: fixed;
    pointer-events: none;
    inset: 0;
}

${AppOverlayKey.MediaVlgReveal} {
    opacity: 1;
    transform: translate(0px, 0px);
    
    transition: 
        opacity var(--magic-interval),
        transform var(--magic-interval),
        visibility var(--magic-interval) allow-discrete;
}

@media (max-width: 1000px) {
    ${AppOverlayKey.MediaVlgReveal} {
        transform: var(--slide-up-initial);
        opacity: 0;         
        visibility: hidden;    
    }
}

@starting-style {
    ${AppOverlayKey.MediaVlgReveal} {
        opacity: 0;
        transform: var(--slide-up-initial);
    }
}

"""