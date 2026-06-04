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
    val onFocus: (PointMarker?) -> Unit
) {
    val markers = mutableMapOf<MapMarkerId, PointEntityView>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
    private var visibilityFunction: ((MapMarker) -> Boolean)? = null
    private var focus: PointEntityView? = null
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

    fun applyOpacity(obj: PointEntityView) {
        val visibility = visibilityFunction ?: return
        val isVisible = visibility(obj.entity)
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

    fun removeEntities(entityIds: List<MapMarkerId>) {
        entityIds.forEach { entityId ->
            markers[entityId]?.marker?.remove()
            markers.remove(entityId)
        }
    }

    fun removeEntities(entities: List<MapMarker>) {
        entities.forEach { entity ->
            val entityId = entity.markerId
            markers[entityId]?.marker?.remove()
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

    private fun updateVisibility(entityId: MapMarkerId) {
        val bounds = boundsNow ?: return
        val view = markers[entityId] ?: return
        val isVisible = bounds.contains(view.position)
        view.setIsVisible(isVisible, widget)
    }

    private fun setFocus(entity: PointMarker?) {
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
//        console.log(zoom)
        val altitude = altitudeOf(zoom)
        if (altitude == altitudeNow) return
        val modifiersNow = altitudeNow?.modifiers ?: emptySet()
        val unmodifiers = modifiersNow - altitude.modifiers
        val modifiers = altitude.modifiers - modifiersNow

        windowElement.unmodify(unmodifiers)
        windowElement.modify(modifiers)
        altitudeNow = altitude
//        console.log(altitude.selector)
//        console.log("unmodify: ${unmodifiers.joinToString(", ") { it.value }}")
//        console.log("modify: ${modifiers.joinToString(", ") { it.value }}")
    }

    fun hideLayer(layerId: LayerId) {
        widget.setLayoutProperty(layerId, "visibility", "none")
    }

    fun showLayer(layerId: LayerId) {
        widget.setLayoutProperty(layerId, "visibility", "visible")
    }

    private fun recallObject(entity: PointMarker, center: GeoPoint): PointEntityView? {
        val view = markers[entity.markerId] ?: return null

        // move marker
        view.move(entity.geoPoint)

        // set entity
        val pixelPoint = entity.geoPoint.toPoint(center.lat)
        view.setEntity(entity, pixelPoint)

        return view
    }

    private fun createObject(entity: PointMarker, center: GeoPoint): PointEntityView {
        val pixelPoint = entity.geoPoint.toPoint(center.lat)
        val mapEntityView = entity.toMapEntityView(pixelPoint) {
            setFocus(entity)
        }

        mapEntityView.marker.setLngLat(entity.geoPoint.toLngLat())
        markers[entity.markerId] = mapEntityView
        return mapEntityView
    }
}

typealias LayerId = String

data class TempEntitySet(
    val entities: List<MapMarker>
)