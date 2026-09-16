package streetlight.web.pages

import koala.SvgFile
import koala.modifier.BlurBackdrop
import koala.modifier.Class
import koala.modifier.FadeLoop
import koala.modifier.HeavyCardBg
import koala.modifier.Height5
import koala.modifier.OverflowClip
import koala.modifier.PointerEventsAuto
import koala.modifier.modify
import koala.modifier.stylesheet
import koala.html.Id
import koala.interop.InlineJs
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.setId
import kotlinx.html.FlowContent
import kotlinx.html.onClick

fun FlowContent.starHelmPopover() {
    popover(StarHelm.PopoverId, modify(StarHelm.PopoverClass)) {
        card(modify(StarHelm.PopoverCardClass, HeavyCardBg, BlurBackdrop, OverflowClip, PointerEventsAuto)) {
            setId(StarHelm.StarBarHelm)
            button(SvgFile.LoaderSmall, modify(Height5, FadeLoop)) {
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
    border-radius: 0 0 0 var(--unit-spacing-2);
}

${StarHelm.PopoverClass} {
    position: fixed;
    top: 0;
    right: 0;
}
"""