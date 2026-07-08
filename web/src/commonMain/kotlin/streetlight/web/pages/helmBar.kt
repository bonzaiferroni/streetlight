package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent

fun FlowContent.helmBar() {
    val cardMod = modify(HelmBar.CardClass, BlurBackdrop, PointerEventsAuto, BorderRadius50P, ZenBg, BorderSolid2Px)
    siteMenuPopover()
    starHelm()
    row(HelmBar.StickyBarId, modify(Padding1)) {
        div(HelmBar.SiteHelmButton, cardMod) {
            setAnchorName(SiteHelm.PositionAnchor)
            button(SvgFile.Helm, HelmBar.IconMod + WhiteFg) {
                setPopoverTarget(SiteHelm.Id)
            }
        }
        spacer(modify(Flex1))
        div(cardMod) {
            setAnchorName(StarHelmKey.PositionAnchor)
            button(HelmBar.IconMod) {
                setPopoverTarget(StarHelmKey.PopoverId)
                starBadge()
            }
        }
    }

    stylesheet(StickyBarCss)
}

object HelmBar {
    val StickyBarId = Id("helm-bar")
    val CardClass = Class("helm-card")
    val IconMod = modify(Height6, Aspect1, DisplayFlex)
    val SiteHelmButton = Id("site-helm-button")
}

// language="CSS"
val StickyBarCss get() = with(HelmBar) { """
${AppOverlay.RevealLeftPanel} $SiteHelmButton {
    display: none;
} 
""" }