package koala.model

import kampfire.model.GeoRect
import kampfire.model.DEG_TO_RAD
import kampfire.model.GeoPoint
import kampfire.model.toPlanarPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

internal class GeoLayerRender(
    val layer: GeoLayer,
    val jsMap: maplibregl.Map,
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
        val pointBuffer = mutableMapOf<MarkerId, PointMarkerElement>()
        val refLatitude = markers.sumOf { it.geoPoint.lat } / markers.size
        refLatitudeNow = refLatitude

        // add or update points
        markers.forEach { marker ->
            val planarPoint = marker.geoPoint.toPlanarPoint(refLatitude)

            val render = pointRenders[marker.markerId]?.also { render ->
                // update render
                render.update(marker, planarPoint)
            } ?: marker.toPointRender(planarPoint) {
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

    private fun clusterPoints() {
        val clusterRadiusPx = layer.config.clusterRadiusPx ?: return
        val zoom = zoomNow ?: return
        val refLatitude = refLatitudeNow ?: return
        val clusterRadiusDegrees = clusterRadiusPx * degreesPerPixelOf(zoom)
        pointClusters = defineClusters(clusterRadiusDegrees, refLatitude)
        applyClusters(pointClusters)
    }

    private fun defineClusters(clusterRadiusDegrees: Double, refLatitude: Double): Map<MarkerId, PointCluster?> {
        val halfHeightAtEquator = clusterRadiusDegrees * CLUSTER_HEIGHT_RATIO / 2
        val halfWidth = halfHeightAtEquator * CLUSTER_ASPECT
        val halfHeight = halfHeightAtEquator * cos(refLatitude * DEG_TO_RAD)
        val clusters = mutableMapOf<MarkerId, PointCluster?>()
        val clustered = mutableSetOf<MarkerId>()
        val currentSet = mutableSetOf<MarkerId>()

        pointRenders.forEach { (markerId, render) ->
            if (markerId in clustered) return@forEach

            val anchor = render.position
            val clusterRect = GeoRect(
                sw = GeoPoint(lng = anchor.lng - halfWidth, lat = anchor.lat - halfHeight),
                ne = GeoPoint(lng = anchor.lng + halfWidth, lat = anchor.lat + halfHeight),
            )

            pointRenders.forEach { (otherId, otherRender) ->
                if (otherId == markerId || otherId in clustered) return@forEach
                if (!clusterRect.contains(otherRender.position)) return@forEach
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
            render.setCluster(cluster)
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

    private fun cullOutsideBounds(render: PointMarkerElement) {
        val bounds = view ?: return
        val isVisible = bounds.contains(render.position)
        render.setIsVisible(isVisible, jsMap)
    }
}

fun degreesPerPixelOf(zoom: Float): Double = 360.0 / (2.0f.pow(zoom) * MAPLIBRE_TILE_SIZE)

const val MAPLIBRE_TILE_SIZE = 512.0

// full height of the cluster rect as a fraction of the cluster radius
private const val CLUSTER_HEIGHT_RATIO = 1
// width of the cluster rect as a multiple of its height
private const val CLUSTER_ASPECT = 3.0

internal class PointCluster(
    val principalId: MarkerId,
    val markerIds: Set<MarkerId>
)
