package streetlight.web.pages

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.AlignItemsEnd
import koala.css.BlurBackdrop
import koala.css.BorderDashed2Px
import koala.css.BorderRadius50P
import koala.css.Class
import koala.css.FadeLoop
import koala.css.HeavyCardBg
import koala.css.Magic
import koala.css.PaddingLeft3
import koala.css.SlideLeft
import koala.css.SpinLoop
import koala.css.modify
import koala.css.stylesheet
import koala.html.Id
import koala.html.InlineJs
import koala.html.action
import koala.html.button
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.icon
import koala.html.popover
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import kotlinx.html.style
import streetlight.web.SiteConfigRoute

fun FlowContent.starHelm() {
    val closePopover = InlineJs.closePopover(StarHelmKey.Id)
    popover(StarHelmKey.Id, null, modify(StarHelmKey.PopoverClass, Magic, SlideLeft)) {
        card(modify(StarHelmKey.PopoverCardClass, HeavyCardBg, BlurBackdrop)) {
            column(modify(PaddingLeft3, AlignItemsEnd)) {
                val rowMod = modify(AlignItemsCenter)
                row(rowMod) {
                    heading3("You")
                    // user badge goes here
                    // make it glow
                    button(modify(HelmBarKey.ButtonMod, FadeLoop, BorderDashed2Px, BorderRadius50P)) {
                        onClick = closePopover

                        starBadge()
                    }
                }
                // sign in goes here

                // calendar route goes here

                action(SiteConfigRoute) { // filler content
                    onClick = closePopover
                    row(rowMod) {
                        textBlock("Settings")
                        icon(SvgFile.Settings, HelmBarKey.ButtonMod)
                    }
                }
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