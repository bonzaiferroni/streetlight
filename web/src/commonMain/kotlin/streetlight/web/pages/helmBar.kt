package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent

fun FlowContent.helmBar() {
    val cardMod = modify(HelmBarKey.CardClass, BlurBackdrop, PointerEventsAuto, BorderRadius50P, ZenBg, BorderSolid2Px)
    siteHelm()
    starHelm()
    row(HelmBarKey.StickyBarId, modify(JustifyContentSpaceBetween, Padding1)) {
        div(cardMod) {
            setAnchorName(SiteHelm.PositionAnchor)
            button(SvgFile.Helm, HelmBarKey.IconMod + WhiteFg) {
                setPopoverTarget(SiteHelm.Id)
            }
        }
        div(cardMod) {
            setAnchorName(StarHelmKey.PositionAnchor)
            button(HelmBarKey.IconMod) {
                setPopoverTarget(StarHelmKey.PopoverId)
                starBadge()
            }
        }
    }

    stylesheet(StickyBarCss)
}

object HelmBarKey {
    val StickyBarId = Id("helm-bar")
    val CardClass = Class("helm-card")
    val IconMod = modify(Height6, Aspect1, DisplayFlex)
}

// language="CSS"
val StickyBarCss get() = """
${HelmBarKey.StickyBarId} {
}
"""