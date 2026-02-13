@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.model

import kampfire.model.GeoPoint

interface LineEntity: MapEntity {
    val points: List<GeoPoint>
    val layerId: LayerId
    val color: String get() = "#4fd1c5"
    val width: Int get() = 2
    val joinShape: String get() = "round"
    val capShape: String get() = "round"
}

data class MapLine(
    val feature: dynamic,
    val entity: LineEntity,
)

fun MapContext.showLines(entities: List<LineEntity>) {
    val layerIds = entities.mapNotNull { entity ->
        val lineSet = lineLayers.getOrPut(entity.layerId) { mutableListOf() }
        if (lineSet.any { it.entity.entityId  == entity.entityId} ) return@mapNotNull null
        val line = entity.createLine()
        lineSet.add(line)
        entity.layerId
    }.toSet()

    layerIds.forEach { layerId ->
        val lineSet = lineLayers.getValue(layerId)
        val lines = lineSet.map { it.feature }.toJsArray()
        val sourceData = jsObject {
            type = "FeatureCollection"
            features = lines
        }

        if (layers.contains(layerId)) {
            console.log("adding to existing layer")
            widget.getSource(layerId).setData(sourceData)
        } else {
            val entity = lineLayers.getValue(layerId).first().entity
            console.log("creating layer")
            val sourceObj = jsObject {
                type = "geojson"
                data = sourceData
            }
            val layerObj = jsObject {
                id = layerId
                type = "line"
                source = layerId
                paint = jsObject(
                    "line-color" to entity.color,
                    "line-width" to entity.width
                )
                layout = jsObject(
                    "line-join" to entity.joinShape,
                    "line-cap" to entity.capShape
                )
            }
            widget.addSource(layerId, sourceObj)
            widget.addLayer(layerObj)
            layers.add(layerId)
        }
    }
}

@Suppress("DuplicatedCode")
fun LineEntity.createLine(): MapLine {
    val line = points.map { arrayOf(it.lng, it.lat) }.toJsArray()
    val feature = jsObject {
        type = "Feature"
        properties = jsObject { }
        geometry = jsObject {
            type = "LineString"
            coordinates = line
        }
    }
    return MapLine(feature, this)
}

//    val lines = entity.points.map { point ->
//
//    }.toJsArray()

//
//
//    maplibre.addSource("routes", sourceObj);
//    maplibre.addLayer(layerObj)