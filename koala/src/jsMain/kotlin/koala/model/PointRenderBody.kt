package koala.model

import web.html.HTMLElement

sealed interface PointRenderBody {
    val element: HTMLElement
    val labelElement: HTMLElement?
    val clusterElement: HTMLElement? get() = null

    fun update(marker: PointMarker)
}

