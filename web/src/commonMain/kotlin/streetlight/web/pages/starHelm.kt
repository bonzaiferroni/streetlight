package streetlight.web.pages

import koala.SvgFile
import koala.css.BlurBackdrop
import koala.css.Class
import koala.css.FadeLoop
import koala.css.HeavyCardBg
import koala.css.Height5
import koala.css.Magic
import koala.css.OverflowClip
import koala.css.SlideLeft
import koala.css.modify
import koala.css.stylesheet
import koala.html.Id
import koala.html.InlineJs
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.setId
import kotlinx.html.FlowContent
import kotlinx.html.onClick

fun FlowContent.starHelm() {
    popover(StarHelmKey.Id, null, modify(StarHelmKey.PopoverClass, Magic, SlideLeft)) {
        card(modify(StarHelmKey.PopoverCardClass, HeavyCardBg, BlurBackdrop, OverflowClip)) {
            setId(StarHelmKey.ContentId)
            button(SvgFile.LoaderSmall, modify(Height5, FadeLoop)) {
                onClick = StarHelmKey.ClosePopover
            }
        }
    }

    stylesheet(StarHelmCss)
}

object StarHelmKey {
    val Id = Id("star-helm")
    val PositionAnchor = Id.toPositionAnchor()
    val PopoverClass = Class("star-helm-popover")
    val PopoverCardClass = Class("star-helm-popover-card")
    val ClosePopover = InlineJs.closePopover(Id)
    val ContentId = Id("star-helm-content")
}

// language="CSS"
val StarHelmCss get() = """
${StarHelmKey.PopoverCardClass} {
    border-radius: 0 0 0 var(--unit-spacing-2);
}

${StarHelmKey.PopoverClass} {
    position: fixed;
    top: 0;
    right: 0;
}
"""