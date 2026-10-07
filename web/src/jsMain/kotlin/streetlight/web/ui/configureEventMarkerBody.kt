package streetlight.web.ui

import koala.html.row
import koala.html.textBlock
import koala.model.ThumbMarker
import koala.model.markerBodyRow
import koala.model.markerLabel
import koala.model.markerLabelColumn
import koala.model.markerThumb
import koala.modifier.Bold
import koala.modifier.ColorSchemeFg
import koala.modifier.Css
import koala.modifier.Css.MaxWidth
import koala.modifier.OpacityHalf
import koala.modifier.OpacityHigh
import koala.modifier.TextOverflowEllipses
import koala.modifier.TextSmall
import koala.modifier.invoke
import koala.modifier.modify
import kotlinx.css.em
import kotlinx.html.DIV
import streetlight.web.layouts.ThemeColor

/** The body of an event [marker], its sublabel reading tag • time above [locationName]. */
internal fun DIV.configureEventMarkerBody(marker: ThumbMarker, locationName: String?) {
    with(marker) {
        markerBodyRow {
            markerThumb()
            markerLabelColumn {
                markerLabel()
                if (typeLabel != null || sublabel != null) {
                    row(modify(TextSmall, Css.Gap.of(0.25.em))) {
                        typeLabel?.let {
                            textBlock(it, modify(ColorSchemeFg, Bold))
                        }
                        sublabel?.let {
                            if (typeLabel != null) separator()
                            textBlock(it, OpacityHigh)
                        }
                    }
                }
                locationName?.let {
                    textBlock(it, modify(TextSmall, LocationScheme, ColorSchemeFg, Bold, MaxWidth(24), TextOverflowEllipses))
                }
            }
        }
    }
}

private val LocationScheme = Css.ColorScheme.of(ThemeColor.Location.cssValue)

private fun DIV.separator() = textBlock("•", OpacityHalf)
