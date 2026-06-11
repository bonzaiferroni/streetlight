package koala.model

import org.w3c.dom.HTMLElement

sealed interface PointRenderBody {
    val element: HTMLElement
    val labelElement: HTMLElement?
    val clusterElement: HTMLElement? get() = null

    fun update(marker: PointMarker)
}

