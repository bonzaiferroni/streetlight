package streetlight.web.model

import koala.model.GeoLayerConfig
import koala.model.GeoLayerId

/** The map layers the site draws on. */
object MarkerLayerConfig {
    val Transit = GeoLayerConfig(GeoLayerId("transit"))
    val Markers = GeoLayerConfig(GeoLayerId("markers"), 48)
}