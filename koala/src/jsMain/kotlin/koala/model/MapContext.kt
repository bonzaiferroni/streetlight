package koala.model

import koala.external.maplibregl

class MapContext(
    val widget: maplibregl.Map,
) {
    val markers = mutableMapOf<MapEntityId, MapObject>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
}

typealias LayerId = String

fun MapContext.collectEntities(entities: List<MapEntity>) {
    entities.forEach { entity ->
        when (entity) {
            is PointEntity -> {
                val markerElement = recallObject(entity) ?: createObject(entity)
                markerElement.setAttributes(entity)
            }
            is LineEntity -> {
                showLines(listOf(entity))
            }
        }
    }
}