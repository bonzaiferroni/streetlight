package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.toPlanarPoint
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class GeoLayerRender(
    val layer: GeoLayer,
    val jsMap: maplibregl.Map,
    val scope: CoroutineScope,
    val onFocus: (PointMarker?) -> Unit
) {
    val pointRenders = mutableMapOf<MarkerId, PointRender>()
    val lineRenders = mutableMapOf<MarkerId, LineRender>()

    private var boundsNow: GeoBounds? = null
    private val job = scope.launch {
        launch {
            layer.pointsFlow.collect(::setPoints)
        }

        launch {
            layer.linesFlow.collect(::setLines)
        }
    }

    fun setPoints(markers: List<PointMarker>) {
        console.log("setting points: ${layer.layerId}")
        val center = jsMap.getCenter().toGeoPoint()

        // add or update points
        markers.forEach { point ->
            val render = recallPoint(point, center) ?: createPoint(point, center)
            render.setAttributes(point)
            updateVisibility(render)
        }

        // remove cached points not in list
        pointRenders.toList().forEach { (key, render) ->
            if (markers.any { it.markerId == key }) return@forEach
            render.dispose()
            pointRenders.remove(key)
        }
    }

    fun setLines(markers: List<LineMarker>) {
        markers.forEach { line ->
            if (lineRenders.any { it.key == line.markerId }) return@forEach
            val render = line.toLineRender(jsMap)
            jsMap.addSource(line.markerId, render.mapSource)
            jsMap.addLayer(render.layerSpecification)
            lineRenders[line.markerId] = render
        }

        lineRenders.toList().forEach { (key, render) ->
            if (markers.any { it.markerId == key}) return@forEach
            jsMap.removeLayer(render.marker.markerId)
            jsMap.removeSource(render.marker.markerId)
            lineRenders.remove(render.marker.markerId)
        }
    }

    fun moveEntity(movement: MarkerMovement) {
        val view = pointRenders[movement.markerId] ?: return
        view.move(movement.position)
        console.log("moved to ${movement.position}")
    }

    internal fun setBounds(bounds: GeoBounds) {
        boundsNow = bounds
        // set marker visibility
        pointRenders.forEach {
            updateVisibility(it.value)
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

    private fun recallPoint(marker: PointMarker, center: GeoPoint): PointRender? {
        val view = pointRenders[marker.markerId] ?: return null

        // move marker
        view.move(marker.geoPoint)

        // set entity
        val planarPoint = marker.geoPoint.toPlanarPoint(center.lat)
        view.setEntity(marker, planarPoint)

        return view
    }

    private fun createPoint(point: PointMarker, center: GeoPoint): PointRender {
        val pixelPoint = point.geoPoint.toPlanarPoint(center.lat)
        val markerView = point.toPointView(pixelPoint) {
            onFocus(point)
        }

        markerView.jsMarker.setLngLat(point.geoPoint.toLngLat())
        pointRenders[point.markerId] = markerView
        return markerView
    }

    private fun updateVisibility(render: PointRender) {
        val bounds = boundsNow ?: return
        val isVisible = bounds.contains(render.position)
        render.setIsVisible(isVisible, jsMap)
    }
}