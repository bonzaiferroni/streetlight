package streetlight.web.pages

import koala.SvgFile
import koala.css.BlurBackdrop
import koala.css.Class
import koala.css.FadeLoop
import koala.css.HeavyCardBg
import koala.css.Height5
import koala.css.Magic
import koala.css.OverflowClip
import koala.css.PointerEventsAuto
import koala.css.SlideLeft
import koala.css.modify
import koala.css.stylesheet
import koala.html.Id
import koala.interop.InlineJs
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.setId
import kotlinx.html.FlowContent
import kotlinx.html.onClick

fun FlowContent.starHelmPopover() {
    popover(StarHelm.Popover, modify(StarHelm.PopoverClass, Magic, SlideLeft)) {
        card(modify(StarHelm.PopoverCardClass, HeavyCardBg, BlurBackdrop, OverflowClip, PointerEventsAuto)) {
            setId(StarHelm.HelmMenu)
            button(SvgFile.LoaderSmall, modify(Height5, FadeLoop)) {
                onClick = StarHelm.ClosePopover.block
            }
        }
    }

    stylesheet(StarHelmCss)
}

object StarHelm {
    val Popover = Id("star-helm-popover")
    val PositionAnchor = Popover.toPositionAnchor()
    val PopoverClass = Class("star-helm-popover")
    val PopoverCardClass = Class("star-helm-popover-card")
    val ClosePopover = InlineJs.closePopover(Popover)
    val HelmMenu = Id("star-helm-menu")
    val StarMenu = Id("star-bar-menu")
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