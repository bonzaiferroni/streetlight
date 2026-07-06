package streetlight.web.ui

import koala.SvgFile
import koala.css.AccentFg
import koala.css.AlignItemsCenter
import koala.css.Aspect1
import koala.css.BlurBackdrop
import koala.css.Bold
import koala.css.Flex1
import koala.css.Gap0
import koala.css.Gap2
import koala.css.LineHeight115
import koala.css.OpacityHigh
import koala.css.OpacityLow
import koala.css.Padding1
import koala.css.PaperGradientBg
import koala.css.PointerEventsAuto
import koala.css.PrimaryFg
import koala.css.SingleLine
import koala.css.Width5
import koala.css.WidthFitContent
import koala.css.modify
import koala.dom.AppScope
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.icon
import koala.dom.onClick
import koala.dom.row
import koala.dom.textBlock
import koala.html.filigree
import koala.html.heading3
import koala.html.logo
import koala.html.span
import streetlight.web.GalaxyMapRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Earth
import streetlight.web.model.MarkerType

private fun AppScope.boundsHud(model: Earth) {
    flowBlock(model.summaryFlow) { summary ->
        if (summary.isNullOrEmpty()) return@flowBlock
        column(modify(WidthFitContent, Gap0)) {
            filigree {
                textBlock("In View", modify(OpacityHigh))
            }
            row(modify(Gap2)) {
                summary.forEach { (markerType, count) ->
                    textBlock {
                        when (markerType) {
                            MarkerType.Event -> span("Events", modify(AccentFg))
                            MarkerType.Location -> span("Locations", modify(PrimaryFg))
                            MarkerType.Galaxy -> span("Galaxies", modify())
                            MarkerType.Media -> span("Media", modify())
                            MarkerType.City -> span("Cities", modify())
                        }
                        span(" | ", modify(OpacityLow))
                        span(count.toString())
                    }
                }
            }
        }
    }
}

fun AppScope.earthHeaderLegacy(model: Earth) {
    val iconMod = modify(Width5, Aspect1)
    row(modify(EarthStyle.Header, AlignItemsCenter, PaperGradientBg, Padding1, PointerEventsAuto, BlurBackdrop)) {
        flowBlock(model.mapFlow, modify(Flex1)) { map ->
            when (map) {
                null -> row {
                    icon(SvgFile.Helm, iconMod)
                    logo()
                }
                else -> row(modify(AlignItemsCenter)) {
                    icon(SvgFile.ArrowLeft, iconMod).onClick {
                        portal.go(GalaxyMapRoute(null))
                    }
                    heading3(map.title, modify(LineHeight115, SingleLine, Bold))
                }
            }
        }
        icon(SvgFile.GearLarge, iconMod).onClick { portal.go(HomeRoute) }
    }
}