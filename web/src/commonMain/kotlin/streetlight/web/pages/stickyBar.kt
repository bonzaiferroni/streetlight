package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.web.AccountRoute

fun FlowContent.stickyBar() {
    val cardMod = modify(BlurBackdrop, PointerEventsAuto, BorderRadius50P, BlurBg, StickyCardClass)
    val iconMod = modify(Height6, AspectRatio1, DisplayFlex)

    popover(StickyBarKey.HelmId, HelmAnchor, modify(HelmPopoverClass, Magic, SlideRight)) {
        card(modify(HelmMenuClass)) {
            spacer(iconMod)
            textBlock("yer menu")
        }
    }
    row(StickyBarKey.StickyBarId, modify(JustifyContentSpaceBetween)) {
        card(cardMod) {
            setId(StickyBarKey.HelmId)
            setAnchor(HelmAnchor)
            button(SvgFile.Helm, iconMod) {
                setAttribute(Attribute.PopoverTarget, StickyBarKey.HelmId.value)
            }
//            action(SiteConfigRoute, iconMod + OpacityMost) {
//                icon(SvgFile.Helm)
//            }
        }
        card(cardMod) {
            action(AccountRoute, iconMod, id = AppBodyKey.BadgeId) {
                emptyBadge()
            }
        }
    }
}

private val StickyCardClass = Class("sticky-card")
private val HelmMenuClass = Class("helm-menu")
private val HelmPopoverClass = Class("helm-popover")
private val HelmAnchor = Anchor("viewport-anchor")

object StickyBarKey {
    val StickyBarId = Id("sticky-bar")
    val HelmId = Id("helm")
}

// language="CSS"
val StickyBarCss get() = """
${StickyBarKey.StickyBarId} {
    position: fixed;
    pointer-events: none;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 14;
}

$StickyCardClass {
    transition: background-color var(--magic-interval) var(--magic-easing);
}

@media (min-width: ${BODY_WIDTH_PX + 128}px) {
    $StickyCardClass {
        background-color: transparent;
    }
}

$HelmMenuClass {
    border-radius: 0 0 var(--unit-spacing-2) 0;
}
"""