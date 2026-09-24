package streetlight.web.pages

import koala.SvgFile
import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.ui.Screen

/**
 * The bar of the site and star helm buttons, each opening its popover. A button hides while its panel is shown
 * beside the content.
 */
fun FlowContent.helmBar() {
    val cardMod = modify(HelmBar.CardClass, BlurBackdrop, PointerEventsAuto, BorderRadius50P, ZenBg, Outline)
    siteHelmPopover()
    starHelmPopover()
    row(HelmBar.StickyBarId, Padding(1)) {
        div(HelmBar.SiteHelmButton, cardMod) {
            setAnchorName(SiteHelm.PositionAnchor)
            button(SvgFile.Helm, modify(HelmBar.IconMod, WhiteFg)) {
                setPopoverTarget(SiteHelm.popoverId)
            }
        }
        spacer(Flex1)
        div(HelmBar.StarHelmButton, cardMod) {
            setAnchorName(StarHelm.PositionAnchor)
            button(HelmBar.IconMod) {
                setPopoverTarget(StarHelm.PopoverId)
                starBadge()
            }
        }
    }

    stylesheet(StickyBarCss)
}

object HelmBar {
    val StickyBarId = Id("helm-bar")
    val CardClass = Class("helm-card")
    val IconMod = modify(Height(6), Aspect1, DisplayFlex)
    val SiteHelmButton = Id("site-helm-button")
    val StarHelmButton = Id("star-helm-button")
}

// language="CSS"
val StickyBarCss get() = with(HelmBar) { """
body:not(${KoalaBody.ScreenId.to(Screen.Earth.screenId)}) {

    &${AppOverlay.RevealLeftPanel} $SiteHelmButton,
    &${AppOverlay.RevealRightPanel} $StarHelmButton {
        display: none;
    }

    $SiteHelmButton, $StarHelmButton {
        @media (min-width: ${CONTENT_PANEL_WIDTH_PX + SIDE_PANEL_WIDTH_PX * 2}px) {
            display: none;
        }
    }
}
""" }