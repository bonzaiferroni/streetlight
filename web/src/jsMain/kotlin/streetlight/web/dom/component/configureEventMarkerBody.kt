package streetlight.web.dom.component

import koala.component.ListStyleGlyph
import koala.component.bulletsOf
import koala.component.span
import koala.component.textBlock
import koala.model.ThumbMarker
import koala.model.markerBodyRow
import koala.model.markerLabel
import koala.model.markerLabelColumn
import koala.model.markerThumb
import koala.modifier.Bold
import koala.modifier.ColorSchemeFg
import koala.modifier.Css
import koala.modifier.Css.MaxWidth
import koala.modifier.LineHeight115
import koala.modifier.OpacityHalf
import koala.modifier.OpacityHigh
import koala.modifier.PaddingLeft
import koala.modifier.TextOverflowEllipses
import koala.modifier.TextSmall
import koala.modifier.invoke
import koala.modifier.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.EntityType
import streetlight.web.model.cssValue

/** The body of an event [marker], its sublabel reading tag · time bulleted above [locationName]. */
internal fun DIV.configureEventMarkerBody(marker: ThumbMarker, locationName: String?) {
    with(marker) {
        markerBodyRow {
            markerThumb()
            markerLabelColumn {
                markerLabel()
                val lines = buildList<FlowContent.() -> Unit> {
                    if (typeLabel != null || sublabel != null) add {
                        textBlock {
                            typeLabel?.let { span(it, modify(ColorSchemeFg, Bold)) }
                            if (typeLabel != null && sublabel != null) span(" · ", OpacityHalf)
                            sublabel?.let { span(it, OpacityHigh) }
                        }
                    }
                    locationName?.let {
                        add { textBlock(it, modify(LocationScheme, ColorSchemeFg, Bold, MaxWidth(28), TextOverflowEllipses)) }
                    }
                }
                if (lines.isNotEmpty()) bulletsOf(modify(PaddingLeft(2), TextSmall, LineHeight115, ListStyleGlyph("›")), *lines.toTypedArray())
            }
        }
    }
}

private val LocationScheme = Css.ColorScheme.of(EntityType.Location.cssValue)
