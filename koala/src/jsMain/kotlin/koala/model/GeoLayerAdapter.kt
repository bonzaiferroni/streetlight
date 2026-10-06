package koala.model

import kampfire.model.GeoRect
import kampfire.model.DEG_TO_RAD
import kampfire.model.distanceSquaredTo
import kampfire.model.toPlanarPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
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

    private var pointClusters: Map<MarkerId, PointCluster?> = emptyMap()

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
                    val focus = pointClusters[marker.markerId]?.markerIds?.mapNotNull {
                        pointRenders[it]?.marker
                    }?.let{ ClusterFocus(marker, it) } ?: MarkerFocus(marker)
                    onFocus(focus)
                }
                pointBuffer[marker.markerId] = render
                cullOutsideBounds(render)
            }

            // remove cached points not in list
            pointRenders.forEach { (key, render) ->
                if (markers.any { it.markerId == key }) return@forEach
                if (render.isFocused) onFocus(null)
                render.dispose()
            }

            pointRenders = pointBuffer

            clusterPoints()
        }
    }

    context(widget: maplibregl.Map)
    private fun clusterPoints() {
        val clusterRadiusPx = layer.config.clusterRadiusPx ?: return
        val zoom = zoomNow ?: return
        val refLatitude = refLatitudeNow ?: return
        val clusterRadiusMetersSq = clusterRadiusMetersOf(zoom, refLatitude, clusterRadiusPx).let { it * it }
        pointClusters = defineClusters(clusterRadiusMetersSq)
        applyClusters(pointClusters)
    }

    private fun defineClusters(clusterRadiusMetersSq: Double): Map<MarkerId, PointCluster?> {
        val clusters = mutableMapOf<MarkerId, PointCluster?>()
        val clustered = mutableSetOf<MarkerId>()
        val currentSet = mutableSetOf<MarkerId>()

        pointRenders.forEach { (markerId, render) ->
            if (markerId in clustered) return@forEach

            pointRenders.forEach { (otherId, otherRender) ->
                if (otherId == markerId || otherId in clustered) return@forEach
                val distanceSq = render.planarPoint.distanceSquaredTo(otherRender.planarPoint)
                if (distanceSq > clusterRadiusMetersSq) return@forEach
                currentSet.add(otherId)
            }

            when (currentSet.isEmpty()) {
                true -> clusters[markerId] = null
                else -> {
                    currentSet.add(markerId)
                    val cluster = PointCluster(markerId, currentSet.toSet())
                    clustered.addAll(currentSet)
                    currentSet.forEach {
                        clusters[it] = cluster
                    }
                    currentSet.clear()
                }
            }
        }

        return clusters
    }

    context(widget: maplibregl.Map)
    private fun applyClusters(clusters: Map<MarkerId, PointCluster?>) {
        clusters.forEach { (markerId, cluster) ->
            val render = pointRenders[markerId] ?: return@forEach
            render.setCluster(cluster)
        }
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
        val isClusterReady = !isMoving && zoom != zoomNow || abs((zoomNow ?: 0f) - zoom) >= 1

        view = bounds
        isMovingNow = isMoving
        if (isClusterReady) {
            zoomNow = zoom
        }

        if (isClusterReady) clusterPoints()

        // set marker visibility
        pointRenders.forEach {
            cullOutsideBounds(it.value)
        }
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

internal class PointCluster(
    val principalId: MarkerId,
    val markerIds: Set<MarkerId>,
)
