package koala.model

import kampfire.model.GeoPoint
import kampfire.model.regionalDistanceTo
import kampfire.model.toPoint
import koala.external.maplibregl

class MapViewContext(
    val widget: maplibregl.Map,
) {
    val markers = mutableMapOf<MapEntityId, PointEntityView>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
    private var visibility: ((MapEntity) -> Boolean)? = null
    private var focus: PointEntityView? = null
    private var tempSet: TempEntitySet? = null

    fun setVisibility(visibility: ((MapEntity) -> Boolean)?) {
        this.visibility = visibility ?: { true }
        markers.forEach { (_, obj) ->
            applyVisibility(obj)
        }
    }

    fun applyVisibility(obj: PointEntityView) {
        val visibility = visibility ?: return
        val isVisible = visibility(obj.entity)
        obj.setOpacity(if (isVisible) 1f else 0f)
    }

    fun addEntities(entities: List<MapEntity>) {
        entities.forEach { entity ->
            when (entity) {
                is PointEntity -> {
                    val center = widget.getCenter().toGeoPoint()
                    val obj = recallObject(entity, center) ?: createObject(entity, center)
                    obj.setAttributes(entity)
                    applyVisibility(obj)
                }
                is LineEntity -> {
                    showLines(listOf(entity))
                }
            }
        }
    }

    fun removeEntities(entityIds: List<MapEntityId>) {
        entityIds.forEach { entityId ->
            markers[entityId]?.marker?.remove()
            markers.remove(entityId)
        }
    }

    fun removeEntities(entities: List<MapEntity>) {
        entities.forEach { entity ->
            val entityId = entity.entityId
            markers[entityId]?.marker?.remove()
            markers.remove(entityId)
        }
    }

    fun getNearest(center: GeoPoint, zoom: Float): PointEntity? {
        var nearest: PointEntityView? = null
        var nearestDistanceSq = Double.MAX_VALUE
        markers.forEach {
            val view = it.value
            val distanceSq = view.entity.position.regionalDistanceTo(center).meters
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq
                nearest = view
            }
        }
        if (nearest != focus) {
            focus?.unfocus()
            focus = nearest
            nearest?.focus()
        }

        return nearest?.entity
    }

    fun moveEntity(movement: EntityMovement) {
        val view = markers[movement.entityId] ?: return
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

    private fun recallObject(entity: PointEntity, center: GeoPoint): PointEntityView? {
        val view = markers[entity.entityId] ?: return null

        // move marker
        view.move(entity.position)

        // set entity
        val pixelPoint = entity.position.toPoint(center.lat)
        view.setEntity(entity, pixelPoint)

        return view
    }

    private fun createObject(entity: PointEntity, center: GeoPoint): PointEntityView {
        val pixelPoint = entity.position.toPoint(center.lat)
        val mapEntityView = entity.toMapEntityView(pixelPoint)

        mapEntityView.marker.setLngLat(entity.position.toLngLat())
        mapEntityView.marker.addTo(widget)
        markers[entity.entityId] = mapEntityView
        return mapEntityView
    }
}

typealias LayerId = String

data class TempEntitySet(
    val entities: List<MapEntity>
)