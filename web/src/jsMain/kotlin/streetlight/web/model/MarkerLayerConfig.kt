package streetlight.web.model

import koala.model.GeoLayerConfig
import koala.model.GeoLayerId

object MarkerLayerConfig {
    val Transit = GeoLayerConfig(GeoLayerId("transit"))
    val Markers = GeoLayerConfig(GeoLayerId("markers"), 5)
}