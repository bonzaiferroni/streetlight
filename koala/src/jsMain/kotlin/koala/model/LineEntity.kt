@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.model

import kampfire.model.GeoPoint
import koala.external.Feature
import koala.external.FeatureCollection
import koala.external.LayerSpecification
import koala.external.LineLayout
import koala.external.LinePaint
import koala.external.LineStringGeometry
import koala.external.MapSource

interface LineEntity : MapEntity {
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

fun MapViewContext.showLines(entities: List<LineEntity>) {
    val layerIds = entities.mapNotNull { entity ->
        val lineSet = lineLayers.getOrPut(entity.layerId) { mutableListOf() }
        if (lineSet.any { it.entity.entityId == entity.entityId }) return@mapNotNull null
        val line = entity.createLine()
        lineSet.add(line)
        entity.layerId
    }.toSet()

    layerIds.forEach { layerId ->
        val lineSet = lineLayers.getValue(layerId)
        val lines = lineSet.map { it.feature }.toJsArray()
        val sourceData = FeatureCollection(
            type = "FeatureCollection",
            features = lines
        )

        if (layers.contains(layerId)) {
            console.log("adding to existing layer")
            widget.getSource(layerId).setData(sourceData)
        } else {
            val entity = lineLayers.getValue(layerId).first().entity
            console.log("creating layer: $layerId")
            val sourceObj = MapSource(
                type = "geojson",
                data = sourceData
            )
            val layerObj = LayerSpecification(
                id = layerId,
                type = "line",
                source = layerId,
                paint = LinePaint(
                    lineColor = entity.color,
                    lineWidth = entity.width
                ),
                layout = LineLayout(
                    lineJoin = entity.joinShape,
                    lineCap = entity.capShape,
                )
            )
            widget.addSource(layerId, sourceObj)
            widget.addLayer(layerObj)
            layers.add(layerId)
        }
    }
}

fun LineEntity.createLine(): MapLine {
    val line = points.map { arrayOf(it.lng, it.lat) }.toJsArray()
    val feature = Feature(
        type = "Feature",
        geometry = LineStringGeometry(
            type = "LineString",
            coordinates = line
        )
    )
    return MapLine(feature, this)
}

//    val lines = entity.points.map { point ->
//
//    }.toJsArray()

//
//
//    maplibre.addSource("routes", sourceObj);
//    maplibre.addLayer(layerObj)