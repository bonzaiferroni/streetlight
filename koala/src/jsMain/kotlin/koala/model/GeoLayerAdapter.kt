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
    val onFocus: (GeoFocus?) -> Unit
) {
    var pointRenders: Map<MarkerId, PointMarkerElement> = emptyMap()
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
            val refLatitude = markers.sumOf { it.geoPoint.lat } / markers.size
            refLatitudeNow = refLatitude

            // add or update points
            markers.forEach { marker ->
                val planarPoint = marker.geoPoint.toPlanarPoint(refLatitude)

                val render = pointRenders[marker.markerId]?.also { render ->
                    // update render
                    render.update(marker, planarPoint)
                } ?: marker.toPointRender(planarPoint, lightLayer) {
                    val focus = pointRenders[marker.markerId]?.let(::clusterOf)
                        ?.let { ClusterFocus(marker, it) } ?: MarkerFocus(marker)
                    onFocus(focus)
                }
                pointBuffer[marker.markerId] = render
            }

            // remove cached points not in list
            pointRenders.forEach { (key, render) ->
                if (markers.any { it.markerId == key }) return@forEach
                if (render.isFocused) onFocus(null)
                render.dispose()
            }

            pointRenders = pointBuffer

            assignClusterSuperiors()
            applyClusterRadius()
            pointRenders.values.forEach { cullOutsideBounds(it) }
        }
    }

    /** Gives each point the nearest point of higher priority, its cluster superior. */
    private fun assignClusterSuperiors() {
        if (layer.config.clusterRadiusPx == null) return
        val renders = pointRenders.values.toList()

        renders.forEachIndexed { index, render ->
            var clusterSuperior: PointMarkerElement? = null
            var clusterSuperiorDistanceSq = Double.POSITIVE_INFINITY
            for (superiorIndex in 0 until index) {
                val candidate = renders[superiorIndex]
                val distanceSq = render.planarPoint.distanceSquaredTo(candidate.planarPoint)
                if (distanceSq >= clusterSuperiorDistanceSq) continue
                clusterSuperior = candidate
                clusterSuperiorDistanceSq = distanceSq
            }
            render.setClusterSuperior(clusterSuperior, clusterSuperiorDistanceSq)
        }
    }

    context(widget: maplibregl.Map)
    private fun applyClusterRadius() {
        val clusterRadiusPx = layer.config.clusterRadiusPx ?: return
        val zoom = zoomNow ?: return
        val refLatitude = refLatitudeNow ?: return
        val clusterRadiusMetersSq = clusterRadiusMetersOf(zoom, refLatitude, clusterRadiusPx).let { it * it }
        pointRenders.values.forEach { it.setClusterRadius(clusterRadiusMetersSq) }
    }

    /** The markers that [clusterHead] stands for, or `null` when it stands only for itself. */
    private fun clusterOf(clusterHead: PointMarkerElement): List<PointMarker>? {
        val markers = pointRenders.values.filter { it.getClusterHead() == clusterHead }.map { it.marker }
        return markers.takeIf { it.size > 1 }
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
//        val view = pointRenders[movement.markerId] ?: return
//        view.move(movement.position)
//        console.log("moved to ${movement.position}")
//    }

    context(widget: maplibregl.Map)
    internal fun setBounds(bounds: GeoRect, zoom: Float, isMoving: Boolean) {
        view = bounds
        isMovingNow = isMoving
        zoomNow = zoom

        applyClusterRadius()
        pointRenders.values.forEach { cullOutsideBounds(it) }
    }

    internal fun dispose() {
        job.cancel()
        pointRenders.forEach {
            it.value.dispose()
        }

        lineRenders.forEach {
            // td: dispose lines
        }
    }

    context(widget: maplibregl.Map)
    private fun cullOutsideBounds(render: PointMarkerElement) {
        val bounds = view ?: return
        val isVisible = bounds.contains(render.position)
        render.setIsVisible(isVisible)
    }
}

/** The distance in meters that [pixelRadius] covers at [zoom], near the latitude [refLatitude]. */
fun clusterRadiusMetersOf(zoom: Float, refLatitude: Double, pixelRadius: Int): Double {
    val metersPerPixel = (40_075_016.686 * cos(refLatitude * DEG_TO_RAD)) / (2.0f.pow(zoom) * MAPLIBRE_TILE_SIZE)
    return pixelRadius * metersPerPixel
}

const val MAPLIBRE_TILE_SIZE = 512.0
