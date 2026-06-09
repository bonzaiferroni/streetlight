@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.model

import koala.external.Feature
import koala.external.FeatureCollection
import koala.external.LayerSpecification
import koala.external.LineLayout
import koala.external.LinePaint
import koala.external.LineStringGeometry
import koala.external.MapSource
import koala.external.maplibregl

class LineRender(
    val marker: LineMarker,
    val mapSource: MapSource,
    val layerSpecification: LayerSpecification,
    val jsMap: maplibregl.Map
) {
    fun hide() {
        jsMap.setLayoutProperty(marker.markerId, "visibility", "none")
    }

    fun show() {
        jsMap.setLayoutProperty(marker.markerId, "visibility", "visible")
    }
}

fun LineMarker.toLineRender(jsMap: maplibregl.Map): LineRender {
    val lines = lines.map { points ->
        val line = points.map { arrayOf(it.lng, it.lat) }.toJsArray()
        Feature(
            type = "Feature",
            geometry = LineStringGeometry(
                type = "LineString",
                coordinates = line
            )
        )
    }.toJsArray()

    val featureCollection = FeatureCollection(
        type = "FeatureCollection",
        features = lines
    )

    val mapSource = MapSource(
        type = "geojson",
        data = featureCollection
    )

    val layerSpecification = LayerSpecification(
        id = markerId,
        type = "line",
        source = markerId,
        paint = LinePaint(
            lineColor = color,
            lineWidth = width
        ),
        layout = LineLayout(
            lineJoin = joinShape,
            lineCap = capShape,
        )
    )

    return LineRender(this, mapSource, layerSpecification, jsMap)
}