package streetlight.web.pages

import koala.Svg
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.web.CityListRoute
import streetlight.web.FrontDeskRoute
import streetlight.web.GalaxyListRoute
import streetlight.web.GalaxyMapRoute
import streetlight.web.HomeRoute
import streetlight.web.SiteConfigRoute
import streetlight.web.doc.SiteDoc
import streetlight.web.layouts.route

fun FlowContent.siteMenuPopover() {
    popover(SiteHelm.Id, SiteHelm.PositionAnchor, modify(SiteHelm.PopoverClass, Magic, SlideRight)) {
        card(modify(SiteHelm.Container, HeavyCardBg, BlurBackdrop, PointerEventsAuto)) {
            column(modify(PaddingRight1)) {
                row(modify(AlignItemsCenter)) {
                    button(SvgFile.Helm, modify(HelmBar.IconMod, SpinLoop, BorderDashed2Px, BorderRadius50P)) {
                        onClick = SiteHelm.closePopover
                    }
                    navigation(HomeRoute) {
                        logo(modify(Height5))
                    }
                }
                siteMenuItems()
            }
        }
    }

    stylesheet(SiteHelmCss)
}

fun FlowContent.siteMenuSidebar() {
    column {
        filigree {
            navigation(HomeRoute) {
                logo(modify(Height5))
            }
        }
        siteMenuItems()
    }
}

fun FlowContent.siteMenuItems() {
    item("Earth", GalaxyMapRoute(null), SvgFile.Earth)
    item("Galaxies", GalaxyListRoute, SvgFile.Satellite)
    item("Cities", CityListRoute, SvgFile.CityLarge)
    // radio
    label("meta")
    item("Help & Feedback", FrontDeskRoute, SvgFile.QuestionLarge)
    item("Status", HomeRoute, SvgFile.ChartLarge) // td
    item("Support", HomeRoute, SvgFile.HeartHandshake) // td
    item("Privacy", SiteDoc.Privacy.route, SvgFile.EyeClosed)
    label("config")
    item("Settings", SiteConfigRoute, SvgFile.GearLarge)
    row(SiteHelm.rowMod + ThemeToggle) {
        onClick = KoalaFun.ToggleTheme.invoke()
        icon(SvgFile.Sun, HelmBar.IconMod)
        textBlock("Theme")
    }
}

private fun FlowContent.label(label: String) {
    filigree {
        textBlock(label, modify(TextSmall, TextTransformUppercase, OpacityHalf))
    }
}

private fun FlowContent.item(label: String, route: AppRoute, svg: Svg) {
    navigation(route) {
        onClick = SiteHelm.closePopover
        row(SiteHelm.rowMod) {
            icon(svg, HelmBar.IconMod)
            textBlock(label)
        }
    }
}

object SiteHelm {
    val Id = Id("site-helm")
    val PositionAnchor = Id.toPositionAnchor()
    val Container = Class("site-helm-menu")
    val PopoverClass = Class("site-helm-popover")

    val closePopover = InlineJs.closePopover(Id)
    val rowMod = modify(AlignItemsCenter)
}

private val ThemeToggle = Class("theme-toggle")

// language="CSS"
val SiteHelmCss get() = """
${SiteHelm.Container} {
    border-radius: 0 0 var(--unit-spacing-2) 0;
    max-height: 100vh;
    overflow-y: auto;
}

${SiteHelm.PopoverClass} {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
}

$DayTheme $ThemeToggle ${IconKey.Class} {
    ${Property.MaskUrl.to(SvgFile.Moon)} !important;
}
"""