package koala.model

import koala.modifier.*
import koala.dom.AppendScope
import koala.dom.row
import koala.dom.textBlock
import koala.html.box
import koala.html.column
import koala.html.icon
import koala.html.span
import kotlinx.html.js.img
import web.html.HTMLElement

internal class StaticMarkerBody(
    override val element: HTMLElement,
): PointMarkerBody {
    override val labelElement: HTMLElement? get() = null

    override fun update(marker: PointMarker) {
        // val marker = marker as FeatureMarker
    }
}

internal fun AppendScope.configureThumbMarker(marker: ThumbMarker): StaticMarkerBody {
    with(marker) {
        // td: declare border radius in stylesheet
        val element = row(modify(MarkerStyle.Body, Gap2Px, AlignItemsCenter, TextShadow)) {
            themeColor?.let {
                setStyle(Css.ColorScheme.of(it))
            }

            box(MarkerStyle.Thumb) {
                img {
                    src = thumbUrl.value
                }
            }

            column(modify(Gap0, LineHeight115, WhiteSpaceNoWrap)) {
                label?.let {
                    textBlock(it, MarkerStyle.LabelMod)
                }
                if (sublabel != null || typeLabel != null) {
                    textBlock(mod = TextSmall) {
                        typeLabel?.let {
                            span(it, modify(ColorSchemeFg, Bold))
                        }
                        sublabel?.let {
                            if (typeLabel != null) span(" • ", OpacityHalf)
                            span(it, OpacityHigh)
                        }
                    }
                }
            }
        }

        return StaticMarkerBody(element)
    }
}

internal fun AppendScope.configureIconMarker(marker: IconMarker): StaticMarkerBody {
    with(marker) {
        val element = row(modify(MarkerStyle.Body, Gap2Px, AlignItemsCenter, BorderRadius3, WhiteSpaceNoWrap)) {
            themeColor?.let {
                setStyle(Css.ColorScheme.of(it))
            }

            box(modify(MarkerStyle.Icon)) {
                icon(marker.svg, modify(SmallIconHeight, PlaceSelfCenter, ColorSchemeFg))
            }

            label?.let {
                textBlock(it, MarkerStyle.LabelMod)
            }
        }

        return StaticMarkerBody(element)
    }
}