package koala.model

import koala.css.*
import koala.dom.DOMContext
import koala.dom.row
import koala.html.column
import koala.html.heading5
import kotlinx.html.js.img
import org.w3c.dom.HTMLElement

internal class ThumbRenderBody(
    override val element: HTMLElement,
): PointRenderBody {
    override val labelElement: HTMLElement? get() = null

    override fun update(marker: PointMarker) {
        val marker = marker as ThumbMarker
    }
}

internal fun DOMContext.configureThumbRender(marker: ThumbMarker): ThumbRenderBody {
    with(marker) {
        // td: declare border radius in stylesheet
        val element = row(modify(MarkerStyle.Body, AlignItemsCenter, BorderRadius3, CardGradientBg, GapHalf)) {
            img {
                addModifiers(MarkerStyle.Thumb)
                src = thumbUrl.value
            }

            labelContent?.let {
                it()
            }
        }

        return ThumbRenderBody(element)
    }
}