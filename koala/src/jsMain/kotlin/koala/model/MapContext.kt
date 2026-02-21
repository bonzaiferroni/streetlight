package koala.model

import koala.external.maplibregl

class MapContext(
    val widget: maplibregl.Map,
) {
    val markers = mutableMapOf<MapEntityId, MapObject>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
    private var visibility: ((MapEntity) -> Boolean)? = null

    fun setVisibility(visibility: ((MapEntity) -> Boolean)?) {
        this.visibility = visibility ?: { true }
        markers.forEach { (_, obj) ->
            applyVisibility(obj)
        }
    }

    fun applyVisibility(obj: MapObject) {
        val visibility = visibility ?: return
        val isVisible = visibility(obj.entity)
        obj.setOpacity(if (isVisible) 1f else 0f)
    }

    fun addEntities(entities: List<MapEntity>) {
        entities.forEach { entity ->
            when (entity) {
                is PointEntity -> {
                    val obj = recallObject(entity) ?: createObject(entity)
                    obj.setAttributes(entity)
                    applyVisibility(obj)
                }
                is LineEntity -> {
                    showLines(listOf(entity))
                }
            }
        }
    }
}

typealias LayerId = String