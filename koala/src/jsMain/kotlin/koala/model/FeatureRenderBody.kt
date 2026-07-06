package koala.model

import koala.css.*
import koala.dom.TagScope
import koala.dom.row
import koala.dom.textBlock
import koala.html.box
import koala.html.column
import koala.html.icon
import koala.html.span
import kotlinx.html.js.img
import org.w3c.dom.HTMLElement

internal class FeatureRenderBody(
    override val element: HTMLElement,
    override val clusterElement: HTMLElement,
): PointRenderBody {
    override val labelElement: HTMLElement? get() = null

    override fun update(marker: PointMarker) {
        val marker = marker as ThumbMarker
    }
}

internal fun TagScope.configureThumbRender(marker: ThumbMarker): FeatureRenderBody {
    var clusterElement: HTMLElement? = null

    with(marker) {
        // td: declare border radius in stylesheet
        val element = row(modify(MarkerStyle.Body, AlignItemsCenter, BorderRadius3, PaperGradientBg)) {
            colorScheme?.let {
                setStyle(Property.ColorScheme.to(it))
            }

            box(modify(MarkerStyle.Thumb)) {
                img {
                    src = thumbUrl.value
                }
                box(modify(MarkerStyle.ClusterCount, CardBg)) {
                    clusterElement = textBlock(mod = modify(PlaceSelfCenter, TextLarge, Bold, NightInk, TextShadow))
                }
            }

            column(modify(GapTiny, LineHeight115, WhiteSpaceNoWrap)) {
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

        return FeatureRenderBody(element, clusterElement!!)
    }
}

internal fun TagScope.configureIconRender(marker: IconMarker): FeatureRenderBody {
    var clusterElement: HTMLElement? = null

    with(marker) {
        val element = row(modify(MarkerStyle.Body, GapTiny, AlignItemsCenter, BorderRadius3, PaperGradientBg)) {
            colorScheme?.let {
                setStyle(Property.ColorScheme.to(it))
            }

            box(modify(MarkerStyle.Icon)) {
                icon(marker.svg, modify(Height3, PlaceSelfCenter, ColorSchemeFg))
                box(modify(MarkerStyle.ClusterCount)) {
                    clusterElement = textBlock(mod = modify(PlaceSelfCenter, TextLarge, Bold, NightInk, TextShadow))
                }
            }

            label?.let {
                textBlock(it, modify(Bold, LineHeight115, WhiteSpaceNoWrap, ColorSchemeFg))
            }
        }

        return FeatureRenderBody(element, clusterElement!!)
    }
}