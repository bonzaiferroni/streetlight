package streetlight.web.page

import koala.SvgFile
import koala.modifier.*
import koala.page.Id
import koala.interop.InlineJs
import koala.component.button
import koala.component.card
import koala.component.popover
import koala.page.setId
import kotlinx.html.FlowContent
import kotlinx.html.onClick

/** The star menu as a popover from the helm button. */
fun FlowContent.starHelmPopover() {
    popover(StarHelm.PopoverId, StarHelm.PopoverClass) {
        card(modify(StarHelm.PopoverCardClass, HeavyCardBg, BlurBackdrop, OverflowClip, PointerEventsAuto)) {
            setId(StarHelm.StarBarHelm)
            button(SvgFile.Loader.small, modify(Height(5), FadeLoop)) {
                onClick = StarHelm.ClosePopover.block
            }
        }
    }

    stylesheet(StarHelmCss)
}

object StarHelm {
    val PopoverId = Id("star-helm-popover")
    val PositionAnchor = PopoverId.toPositionAnchor()
    val PopoverClass = Class("star-helm-popover")
    val PopoverCardClass = Class("star-helm-popover-card")
    val ClosePopover = InlineJs.closePopover(PopoverId)
    val StarBarHelm = Id("star-helm-menu")
    val StarPanelHelm = Id("star-panel-menu")
}

// language="CSS"
val StarHelmCss get() = """
${StarHelm.PopoverCardClass} {
    border-radius: 0 0 0 calc(var(--curve-unit) * 2);
}

${StarHelm.PopoverClass} {
    position: fixed;
    top: 0;
    right: 0;
}
"""