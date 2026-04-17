package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent

fun FlowContent.helmBar() {
    val cardMod = modify(HelmBarKey.CardClass, BlurBackdrop, PointerEventsAuto, BorderRadius50P, ZenCardBg)
    siteHelm()
    starHelm()
    row(HelmBarKey.StickyBarId, modify(JustifyContentSpaceBetween)) {
        card(cardMod) {
            setAnchor(SiteHelmKey.PositionAnchor)
            button(SvgFile.Helm, HelmBarKey.IconMod) {
                setPopoverTarget(SiteHelmKey.Id)
            }
        }
        card(cardMod) {
            setAnchor(StarHelmKey.PositionAnchor)
            button(HelmBarKey.IconMod) {
                setPopoverTarget(StarHelmKey.Id)
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

${HelmBarKey.CardClass} {
    transition: background-color var(--magic-interval) var(--magic-easing);
}

@media (min-width: ${BODY_WIDTH_PX + 128}px) {
    ${HelmBarKey.CardClass} {
        background-color: transparent;
    }
}
"""