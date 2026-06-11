package koala.model

import kampfire.model.GeoBounds
import kampfire.model.DEG_TO_RAD
import kampfire.model.distanceSquaredTo
import kampfire.model.toPlanarPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow

internal class GeoLayerRender(
    val layer: GeoLayer,
    val jsMap: maplibregl.Map,
    val scope: CoroutineScope,
    val onFocus: (PointMarker?) -> Unit
) {
    var pointRenders: Map<MarkerId, PointRender> = emptyMap()
        private set
    var lineRenders: Map<MarkerId, LineRender> = emptyMap()
        private set

    private var pointClusters: Map<MarkerId, PointCluster?> = emptyMap()

    private var refLatitudeNow: Double? = null
    private var boundsNow: GeoBounds? = null
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
        val pointBuffer = mutableMapOf<MarkerId, PointRender>()
        val refLatitude = markers.sumOf { it.geoPoint.lat } / markers.size
        refLatitudeNow = refLatitude

        // add or update points
        markers.forEach { marker ->
            val planarPoint = marker.geoPoint.toPlanarPoint(refLatitude)

            val render = pointRenders[marker.markerId]?.also { render ->
                // update render
                render.update(marker, planarPoint)
            } ?: marker.toPointRender(planarPoint) {
                onFocus(marker)
            }
            pointBuffer[marker.markerId] = render
            cullOutsideBounds(render)
        }

        // remove cached points not in list
        pointRenders.forEach { (key, render) ->
            if (markers.any { it.markerId == key }) return@forEach
            render.dispose()
        }

        pointRenders = pointBuffer

        clusterPoints()
    }

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

    private fun applyClusters(clusters: Map<MarkerId, PointCluster?>) {
        clusters.forEach { (markerId, cluster) ->
            val render = pointRenders[markerId] ?: return@forEach
            val isClusterPrincipal = cluster?.let { it.principalId == markerId}
            render.setClustering(isClusterPrincipal)
        }
    }

    fun setLines(markers: List<LineMarker>) {
        val lineBuffer = mutableMapOf<MarkerId, LineRender>()

        markers.forEach { line ->
            val render = lineRenders[line.markerId] ?: line.toLineRender(jsMap).also { render ->
                jsMap.addSource(line.markerId, render.mapSource)
                jsMap.addLayer(render.layerSpecification)
            }
            lineBuffer[line.markerId] = render
        }

        lineRenders.forEach { (key, render) ->
            if (markers.any { it.markerId == key}) return@forEach
            jsMap.removeLayer(render.marker.markerId)
            jsMap.removeSource(render.marker.markerId)
        }

        lineRenders = lineBuffer
    }

//    fun moveEntity(movement: MarkerMovement) {
//        val view = pointRenders[movement.markerId] ?: return
//        view.move(movement.position)
//        console.log("moved to ${movement.position}")
//    }

    internal fun setBounds(bounds: GeoBounds, zoom: Float, isMoving: Boolean) {
        val isClusterReady = !isMoving && zoom != zoomNow

        boundsNow = bounds
        zoomNow = zoom
        isMovingNow = isMoving

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

    private fun cullOutsideBounds(render: PointRender) {
        val bounds = boundsNow ?: return
        val isVisible = bounds.contains(render.position)
        render.setIsVisible(isVisible, jsMap)
    }
}

fun clusterRadiusMetersOf(zoom: Float, refLatitude: Double, pixelRadius: Int): Double {
    val metersPerPixel = (40_075_016.686 * cos(refLatitude * DEG_TO_RAD)) / (2.0f.pow(zoom) * MAPLIBRE_TILE_SIZE)
    return pixelRadius * metersPerPixel
}

const val MAPLIBRE_TILE_SIZE = 512.0

internal class PointCluster(
    val principalId: MarkerId,
    val markerIds: Set<MarkerId>
)