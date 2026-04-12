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
        row(modify(Height6, Padding1)) {
            spacer(modify(Flex1))
            icon(SvgFile.PanelRight, modify(PointerEventsAuto)) {
                onClick = AppOverlayKey.TogglePanel.invoke(
                    AppBodyKey.SpacerRightId.argument,
//                    AppOverlayKey.PanelRightId.argument
                )
            }
        }
    }

    scriptUnsafe(AppOverlayJs)
}

// language="JS"
val AppOverlayJs get() = """

${AppOverlayKey.TogglePanel} {
    document.getElementById($panelArg).classList.toggle(`${Reveal.identifier}`);
}

"""

private val panelArg = "panelId"

object AppOverlayKey {
    val AppOverlayId = Id("app-overlay")
    val TogglePanel = Fun("togglePanel", panelArg)
    val SpacerMiddleId = Id("spacer-middle")
}

// language="CSS"
val AppOverlayCss get() = """
${AppOverlayKey.AppOverlayId} {
    position: fixed;
    pointer-events: none;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 14;
}

"""