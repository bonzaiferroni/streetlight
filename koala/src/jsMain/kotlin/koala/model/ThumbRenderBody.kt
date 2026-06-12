package koala.model

import koala.css.*
import koala.dom.TagScope
import koala.dom.row
import koala.dom.textBlock
import koala.html.box
import kotlinx.html.js.img
import org.w3c.dom.HTMLElement

internal class ThumbRenderBody(
    override val element: HTMLElement,
    override val clusterElement: HTMLElement,
): PointRenderBody {
    override val labelElement: HTMLElement? get() = null

    override fun update(marker: PointMarker) {
        val marker = marker as ThumbMarker
    }
}

internal fun TagScope.configureThumbRender(marker: ThumbMarker): ThumbRenderBody {
    var clusterElement: HTMLElement? = null

    with(marker) {
        // td: declare border radius in stylesheet
        val element = row(modify(MarkerStyle.Body, AlignItemsCenter, BorderRadius3, PaperGradientBg)) {
            box(modify(MarkerStyle.Thumb)) {
                img {
                    src = thumbUrl.value
                }
                box(modify(MarkerStyle.ClusterCount, CardBg)) {
                    clusterElement = textBlock(mod = modify(PlaceSelfCenter, LargeText))
                }
            }

            labelContent?.let {
                it()
            }
        }

        return ThumbRenderBody(element, clusterElement!!)
    }
}