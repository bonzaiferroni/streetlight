package koala.model

/** The map layers the site draws on. */
object MarkerLayerConfig {
    val Transit = GeoLayerConfig(GeoLayerId("transit"))
    val Markers = GeoLayerConfig(GeoLayerId("markers"), 160)
    val StarLocation = GeoLayerConfig(GeoLayerId("user-location"))
}