@file:Suppress("JSUnresolvedReference", "CssInvalidMediaFeature")

package streetlight.web.pages

import koala.css.*
import koala.interop.KoalaFun.initRootModifier
import koala.html.*
import kotlinx.html.FlowContent

fun FlowContent.appOverlay() {
    column(AppOverlay.Container) {
        helmBar()
        spacer(modify(Flex1))
    }
}

val AppOverlayScript get() = with(AppOverlay) {
    jsScriptOf {
        invoke(initRootModifier, RevealLeftPanel)
        invoke(initRootModifier, RevealRightPanel)
    }
}

object AppOverlay {
    val Container = Id("app-overlay")
    val MediaVlgReveal = Class("display-none-below-vlg")
    val RevealLeftPanel = Class("reveal-left-panel")
    val RevealRightPanel = Class("reveal-right-panel")
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

