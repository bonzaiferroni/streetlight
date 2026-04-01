package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.unsafe
import streetlight.web.AccountRoute
import streetlight.web.SiteConfigRoute

fun FlowContent.stickyBar() {
    val cardMod = modify(StickyCardClass, BlurBackdrop, PointerEventsAuto, BorderRadius50P, HeavyCardBg)
    val iconMod = modify(Height6, AspectRatio1, DisplayFlex)
    val closePopover = InlineJs.closePopover(StickyBarKey.HelmId)
    popover(StickyBarKey.HelmId, null, modify(HelmPopoverClass, Magic, SlideRight)) {
        card(modify(HelmCardClass, HeavyCardBg, BlurBackdrop)) {
            column {
                val rowMod = modify(AlignItemsCenter, PaddingRight3)
                row(rowMod) {
                    button(SvgFile.Helm, modify(iconMod, SpinSlow, BorderDashed2Px, BorderRadius50P)) {
                        onClick = closePopover
                    }
                    heading3("Helm")
                }
                action(SiteConfigRoute) {
                    onClick = closePopover
                    row(rowMod) {
                        icon(SvgFile.Settings, iconMod)
                        textBlock("Settings")
                    }
                }

            }
        }
    }
    row(StickyBarKey.StickyBarId, modify(JustifyContentSpaceBetween)) {
        setAnchor(HelmAnchor)
        card(cardMod) {
            setId(StickyBarKey.HelmId)
            button(SvgFile.Helm, iconMod) {
                setAttribute(Attribute.PopoverTarget, StickyBarKey.HelmId.identifier)
            }
        }
        card(cardMod) {
            action(AccountRoute, iconMod, id = AppBodyKey.BadgeId) {
                emptyBadge()
            }
        }
    }

    style {
        unsafe {
            +StickyBarCss
        }
    }

    script {
        unsafe {
            +StickyBarJs
        }
    }
}

private val StickyCardClass = Class("sticky-card")
private val HelmCardClass = Class("helm-menu")
private val HelmPopoverClass = Class("helm-popover")
private val HelmAnchor = Anchor("viewport-anchor")

object StickyBarKey {
    val StickyBarId = Id("sticky-bar")
    val HelmId = Id("helm")
}

// language="JS"
val StickyBarJs get() = """
console.log("ey")
"""

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

$HelmCardClass {
    border-radius: 0 0 var(--unit-spacing-2) 0;
}

$HelmPopoverClass {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
}
"""