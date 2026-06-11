package koala.model

import koala.css.addModifiers
import koala.css.modify
import koala.dom.DOMContext
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
        val element = img {
            src = thumbUrl.value
            addModifiers(modify(MarkerStyle.Body, MarkerStyle.Thumb))
        }

        return ThumbRenderBody(element)
    }
}