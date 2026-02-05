package streetlight.web

class MapContext(
    val widget: maplibregl.Map,
) {
    val markers = mutableMapOf<MapEntityId, MapObject>()
    val lineLayers = mutableMapOf<LayerId, MutableList<MapLine>>()
    val layers = mutableSetOf<LayerId>()
}

typealias LayerId = String

fun MapContext.collectEntity(entity: MapEntity) {
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