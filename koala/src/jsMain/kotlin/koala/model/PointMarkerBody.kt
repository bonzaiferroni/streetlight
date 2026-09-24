package koala.model

import web.html.HTMLElement

/** The elements of a drawn point marker, updated as the marker changes. */
sealed interface PointMarkerBody {
    val element: HTMLElement
    val labelElement: HTMLElement?
    val clusterElement: HTMLElement? get() = null

    fun update(marker: PointMarker)
}

