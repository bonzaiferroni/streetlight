package koala.model

import kampfire.model.GeoRect
import kampfire.model.DEG_TO_RAD
import kampfire.model.distanceSquaredTo
import kampfire.model.toPlanarPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow

internal class GeoLayerAdapter(
    val layer: GeoLayer,
    val widget: maplibregl.Map,
    val lightLayer: LightLayer,
    val scope: CoroutineScope,
    val onFocus: (PointMarker?) -> Unit
) {
    var pointElements: Map<MarkerId, PointMarkerElement> = emptyMap()
        private set
    var lineRenders: Map<MarkerId, LineRender> = emptyMap()
        private set

    private var refLatitudeNow: Double? = null
    private var view: GeoRect? = null
    private var zoomNow: Float? = null
    private var isMovingNow: Boolean = false

    private val job = scope.launch {
        launch {
            layer.pointsFlow.collect(::setPoints)
        }

        launch {
            layer.linesFlow.collect(::setLines)
        }
    }

    fun setPoints(markers: List<PointMarker>) {
        with(widget) {
            val pointBuffer = mutableMapOf<MarkerId, PointMarkerElement>()
            if (markers.isNotEmpty()) refLatitudeNow = markers.sumOf { it.geoPoint.lat } / markers.size
            val refLatitude = refLatitudeNow ?: 0.0

            // add or update points
            markers.forEach { marker ->
                val planarPoint = marker.geoPoint.toPlanarPoint(refLatitude)

                val element = pointElements[marker.markerId]?.also { element ->
                    // update element
                    element.update(marker, planarPoint)
                } ?: marker.toPointElement(planarPoint, lightLayer) {
                    onFocus(marker)
                }
                pointBuffer[marker.markerId] = element
            }

            // remove cached points not in list
            pointElements.forEach { (key, element) ->
                if (markers.any { it.markerId == key }) return@forEach
                if (element.isFocused) onFocus(null)
                element.dispose()
            }

            pointElements = pointBuffer

            assignRevealZooms()
            applyZoom()
            pointElements.values.forEach { cullOutsideBounds(it) }
        }
    }

    /**
     * Gives each point the zoom where it is revealed, stepping in from [CLUSTER_ZOOM_MIN].
     *
     * Each step keeps the points already revealed and reveals, in priority order, those farther than the cluster radius from every revealed point.
     */
    private fun assignRevealZooms() {
        val clusterRadiusPx = layer.config.clusterRadiusPx ?: return
        val refLatitude = refLatitudeNow ?: return
        val hiddenElements = pointElements.values.toMutableList()
        val revealedElements = mutableListOf<PointMarkerElement>()

        var zoom = CLUSTER_ZOOM_MIN
        while (hiddenElements.isNotEmpty() && zoom <= CLUSTER_ZOOM_MAX) {
            val clusterRadiusMetersSq = clusterRadiusMetersOf(zoom, refLatitude, clusterRadiusPx).let { it * it }
            hiddenElements.removeAll { element ->
                val isCovered = revealedElements.any { element.planarPoint.distanceSquaredTo(it.planarPoint) <= clusterRadiusMetersSq }
                if (isCovered) return@removeAll false
                element.setRevealZoom(zoom)
                revealedElements.add(element)
                true
            }
            zoom += CLUSTER_ZOOM_STEP
        }

        hiddenElements.forEach { it.setRevealZoom(Float.POSITIVE_INFINITY) }
    }

    context(widget: maplibregl.Map)
    private fun applyZoom() {
        val zoom = zoomNow ?: return
        pointElements.values.forEach { it.setZoom(zoom) }
    }

    fun setLines(markers: List<LineMarker>) {
        val lineBuffer = mutableMapOf<MarkerId, LineRender>()

        markers.forEach { line ->
            val render = lineRenders[line.markerId] ?: line.toLineRender(widget).also { render ->
                widget.addSource(line.markerId, render.mapSource)
                widget.addLayer(render.layerSpecification)
            }
            lineBuffer[line.markerId] = render
        }

        lineRenders.forEach { (key, render) ->
            if (markers.any { it.markerId == key}) return@forEach
            widget.removeLayer(render.marker.markerId)
            widget.removeSource(render.marker.markerId)
        }

        lineRenders = lineBuffer
    }

//    fun moveEntity(movement: MarkerMovement) {
//        val view = pointElements[movement.markerId] ?: return
//        view.move(movement.position)
//        console.log("moved to ${movement.position}")
//    }

    context(widget: maplibregl.Map)
    internal fun setBounds(bounds: GeoRect, zoom: Float, isMoving: Boolean) {
        view = bounds
        isMovingNow = isMoving
        zoomNow = zoom

        applyZoom()
        pointElements.values.forEach { cullOutsideBounds(it) }
    }

    internal fun dispose() {
        job.cancel()
        pointElements.forEach {
            it.value.dispose()
        }

        lineRenders.forEach {
            // td: dispose lines
        }
    }

    context(widget: maplibregl.Map)
    private fun cullOutsideBounds(element: PointMarkerElement) {
        val bounds = view ?: return
        val isInBounds = bounds.contains(element.position)
        element.setIsInBounds(isInBounds)
    }
}

/** The distance in meters that [pixelRadius] covers at [zoom], near the latitude [refLatitude]. */
fun clusterRadiusMetersOf(zoom: Float, refLatitude: Double, pixelRadius: Int): Double {
    val metersPerPixel = (40_075_016.686 * cos(refLatitude * DEG_TO_RAD)) / (2.0f.pow(zoom) * MAPLIBRE_TILE_SIZE)
    return pixelRadius * metersPerPixel
}

const val MAPLIBRE_TILE_SIZE = 512.0

private const val CLUSTER_ZOOM_MIN = 0f
private const val CLUSTER_ZOOM_MAX = 22f
private const val CLUSTER_ZOOM_STEP = 0.5f
