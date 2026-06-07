package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.toPoint
import koala.dom.modify
import koala.dom.unmodify
import koala.external.maplibregl
import org.w3c.dom.HTMLElement

class MapViewContext(
    val widget: maplibregl.Map,
    val windowElement: HTMLElement,
    val geoMap: GeoMap,
    val onFocus: (PointMarker?) -> Unit
) {
    val markers = mutableMapOf<MarkerId, MarkerElement>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
    private var visibilityFunction: ((MapMarker) -> Boolean)? = null
    private var focus: MarkerElement? = null
    private var tempSet: TempEntitySet? = null
    private var altitudeNow: Altitude? = null
    private var boundsNow: GeoBounds? = null

    init {
        val element = windowElement.querySelector(".maplibregl-canvas") ?: error("canvas not found")
        element.addEventListener("click", {
            setFocus(null)
        })
    }

    fun setVisibility(visibility: ((MapMarker) -> Boolean)?) {
        this.visibilityFunction = visibility ?: { true }
        markers.forEach { (_, obj) ->
            applyOpacity(obj)
        }
    }

    fun applyOpacity(obj: MarkerElement) {
        val visibility = visibilityFunction ?: return
        val isVisible = visibility(obj.marker)
        obj.setOpacity(if (isVisible) 1f else 0f)
    }

    fun addEntities(entities: List<MapMarker>) {
        entities.forEach { entity ->
            when (entity) {
                is PointMarker -> {
                    val center = widget.getCenter().toGeoPoint()
                    val obj = recallObject(entity, center) ?: createObject(entity, center)
                    obj.setAttributes(entity)
                    applyOpacity(obj)
                    updateVisibility(entity.markerId)
                }
                is LineMarker -> {
                    showLines(listOf(entity))
                }
            }
        }
    }

    fun removeEntities(entityIds: List<MarkerId>) {
        entityIds.forEach { entityId ->
            markers[entityId]?.jsMarker?.remove()
            markers.remove(entityId)
        }
    }

    fun removeEntities(entities: List<MapMarker>) {
        entities.forEach { entity ->
            val entityId = entity.markerId
            markers[entityId]?.jsMarker?.remove()
            markers.remove(entityId)
        }
    }

    fun setBounds(bounds: GeoBounds, center: GeoPoint, zoom: Float) {
        boundsNow = bounds.resizeBy(1.2f)
        // set marker visibility
        markers.forEach {
            updateVisibility(it.key)
        }
    }

//    fun setContextId(contextId: MapContextId) {
//        markers.entries.removeAll {
//            val entityContextId = it.value.entity.contextId ?: return@removeAll false
//            contextId != entityContextId
//        }
//    }

    private fun updateVisibility(entityId: MarkerId) {
        val bounds = boundsNow ?: return
        val view = markers[entityId] ?: return
        val isVisible = bounds.contains(view.position)
        view.setIsVisible(isVisible, widget)
    }

    fun setFocus(entity: PointMarker?) {
        focus?.unfocus()
        val view = entity?.let { markers[it.markerId] }
        view?.focus()
        focus = view
        onFocus(entity)
    }

    fun moveEntity(movement: MarkerMovement) {
        val view = markers[movement.markerId] ?: return
        view.move(movement.position)
        console.log("moved to ${movement.position}")
    }

    fun tempEntitySet(set: TempEntitySet?) {
        val currentSet = tempSet
        if (currentSet != null) {
            removeEntities(currentSet.entities)
        }
        if (set != null) {
            addEntities(set.entities)
        }
        tempSet = set
    }

    fun setAltitude(zoom: Double) {
        val altitude = altitudeOf(zoom)
        if (altitude == altitudeNow) return

        Altitude.entries.forEach {
            when (zoom < it.zoom) {
                true -> windowElement.modify(it)
                else -> windowElement.unmodify(it)
            }
        }
        
        altitudeNow = altitude
    }

    fun hideLayer(layerId: LayerId) {
        widget.setLayoutProperty(layerId, "visibility", "none")
    }

    fun showLayer(layerId: LayerId) {
        widget.setLayoutProperty(layerId, "visibility", "visible")
    }

    private fun recallObject(marker: PointMarker, center: GeoPoint): MarkerElement? {
        val view = markers[marker.markerId] ?: return null

        // move marker
        view.move(marker.geoPoint)

        // set entity
        val pixelPoint = marker.geoPoint.toPoint(center.lat)
        view.setEntity(marker, pixelPoint)

        return view
    }

    private fun createObject(marker: PointMarker, center: GeoPoint): MarkerElement {
        val pixelPoint = marker.geoPoint.toPoint(center.lat)
        val markerView = marker.toMarkerView(pixelPoint) {
            setFocus(marker)
        }

        markerView.jsMarker.setLngLat(marker.geoPoint.toLngLat())
        markers[marker.markerId] = markerView
        return markerView
    }
}

typealias LayerId = String

data class TempEntitySet(
    val entities: List<MapMarker>
)