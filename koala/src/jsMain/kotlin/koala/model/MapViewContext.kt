package koala.model

import kampfire.model.GeoPoint
import kampfire.model.distanceTo
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

    private fun recallObject(entity: PointEntity, center: GeoPoint): PointEntityView? {
        val view = markers[entity.entityId] ?: return null

        // move marker
        val current = view.marker.getLngLat()
        val destination = entity.position.toLngLat()
        val distance = current.distanceTo(destination)
        if (distance > 1) {
            view.marker.move(current, destination)
        } else {
            view.marker.setLngLat(destination)
        }

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