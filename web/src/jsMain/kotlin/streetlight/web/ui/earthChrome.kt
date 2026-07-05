package streetlight.web.ui

import koala.css.AccentFg
import koala.css.AlignItemsStart
import koala.css.BlurBackdrop
import koala.css.Gap0
import koala.css.Gap2
import koala.css.JustifyContentEnd
import koala.css.JustifyContentSpaceBetween
import koala.css.Magic
import koala.css.OpacityHigh
import koala.css.OpacityLow
import koala.css.PointerEventsAuto
import koala.css.PrimaryFg
import koala.css.WidthFitContent
import koala.css.Zen
import koala.css.modify
import koala.dom.AppScope
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.onClick
import koala.dom.row
import koala.dom.textBlock
import koala.html.filigree
import koala.html.span
import streetlight.web.model.Earth
import streetlight.web.model.MarkerType

fun AppScope.earthChrome(model: Earth) {
    column(modify(EarthStyle.Window, EarthStyle.MoveDimmer, JustifyContentSpaceBetween)) {
        row(modify(JustifyContentEnd, AlignItemsStart)) {
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
                                }
                                span(" | ", modify(OpacityLow))
                                span(count.toString())
                            }
                        }
                    }
                }
            }
            button("Show All", modify(Zen, PointerEventsAuto, BlurBackdrop)).onClick(model::showAll)
        }
        flowBlock(model.mapFlow, modify(Magic)) { map ->
            earthRouteMenu(model, map)
        }
    }
}