package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.web.SiteConfigRoute

fun FlowContent.siteHelm() {
    val closePopover = InlineJs.closePopover(SiteHelmKey.Id)
    popover(SiteHelmKey.Id, SiteHelmKey.PositionAnchor, modify(SiteHelmKey.PopoverClass, Magic, SlideRight)) {
        card(modify(SiteHelmKey.CardClass, HeavyCardBg, BlurBackdrop, PointerEventsAuto)) {
            column(modify(PaddingRight3)) {
                val rowMod = modify(AlignItemsCenter)
                row(modify(AlignItemsCenter)) {
                    button(SvgFile.Helm, modify(HelmBarKey.IconMod, SpinLoop, BorderDashed2Px, BorderRadius50P)) {
                        onClick = closePopover
                    }
                    heading3("Helm")
                }
                navigation(SiteConfigRoute) {
                    onClick = closePopover
                    row(rowMod) {
                        icon(SvgFile.Settings, HelmBarKey.IconMod)
                        textBlock("Settings")
                    }
                }
                row(rowMod + ThemeToggle) {
                    onClick = KoalaFun.ToggleTheme.invoke()
                    icon(SvgFile.Sun, HelmBarKey.IconMod)
                    textBlock("Theme")
                }
            }
        }
    }

    stylesheet(SiteHelmCss)
}

object SiteHelmKey {
    val Id = Id("site-helm")
    val PositionAnchor = Id.toPositionAnchor()
    val CardClass = Class("site-helm-menu")
    val PopoverClass = Class("site-helm-popover")
}

private val ThemeToggle = Class("theme-toggle")

// language="CSS"
val SiteHelmCss get() = """
${SiteHelmKey.CardClass} {
    border-radius: 0 0 var(--unit-spacing-2) 0;
}

${SiteHelmKey.PopoverClass} {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
}

$DayTheme $ThemeToggle ${IconKey.Class} {
    ${Property.MaskUrl.to(UrlValue(SvgFile.Moon))} !important;
}
"""