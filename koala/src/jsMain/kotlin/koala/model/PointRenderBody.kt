package koala.model

import org.w3c.dom.HTMLElement

sealed interface PointRenderBody {
    val element: HTMLElement
    val labelElement: HTMLElement?

    fun update(marker: PointMarker)
}

