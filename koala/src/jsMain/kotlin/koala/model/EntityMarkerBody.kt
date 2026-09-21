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

internal class EntityMarkerBody(
    override val element: HTMLElement,
    override val clusterElement: HTMLElement,
): PointMarkerBody {
    override val labelElement: HTMLElement? get() = null

    override fun update(marker: PointMarker) {
        // val marker = marker as FeatureMarker
    }
}

internal fun AppendScope.configureThumbMarker(marker: ThumbMarker): EntityMarkerBody {
    var clusterElement: HTMLElement? = null

    with(marker) {
        // td: declare border radius in stylesheet
        val element = row(modify(MarkerStyle.Body, AlignItemsCenter, BorderRadius3, CardBg)) {
            themeColor?.let {
                setStyle(Css.ColorScheme.of(it))
            }

            box(modify(MarkerStyle.Thumb)) {
                img {
                    src = thumbUrl.value
                }
                box(modify(MarkerStyle.ClusterCount, CardBg)) {
                    clusterElement = textBlock(mod = modify(PlaceSelfCenter, TextLarge, Bold, NightInk, TextShadow))
                }
            }

            column(modify(Gap2Px, LineHeight115, WhiteSpaceNoWrap)) {
                label?.let {
                    textBlock(it, modify(Bold))
                }
                if (sublabel != null || typeLabel != null) {
                    textBlock(mod = modify(TextSmall)) {
                        typeLabel?.let {
                            span(it, modify(ColorSchemeFg, Bold))
                        }
                        sublabel?.let {
                            if (typeLabel != null) span(" • ", modify(OpacityHalf))
                            span(it, modify(OpacityHigh))
                        }
                    }
                }
            }
        }

        return EntityMarkerBody(element, clusterElement!!)
    }
}

internal fun AppendScope.configureIconMarker(marker: IconMarker): EntityMarkerBody {
    var clusterElement: HTMLElement? = null

    with(marker) {
        val element = row(modify(MarkerStyle.Body, Gap2Px, AlignItemsCenter, BorderRadius3, PaperGradientBg)) {
            themeColor?.let {
                setStyle(Css.ColorScheme.of(it))
            }

            box(modify(MarkerStyle.Icon, BorderRadius50P, CardBg, Outline)) {
                icon(marker.svg, modify(SmallIconHeight, PlaceSelfCenter, ColorSchemeFg))
                box(modify(MarkerStyle.ClusterCount)) {
                    clusterElement = textBlock(mod = modify(PlaceSelfCenter, Bold, NightInk, TextShadow))
                }
            }

            label?.let {
                textBlock(it, modify(LineHeight115, WhiteSpaceNoWrap, PaddingRight1, TextShadow))
            }
        }

        return EntityMarkerBody(element, clusterElement!!)
    }
}