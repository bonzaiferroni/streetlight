package streetlight.web

import streetlight.model.data.City
import streetlight.model.data.Galaxy

sealed interface EarthMap {
    val title: String
    val layer: EarthLayer
}

data class GalaxyMap(val galaxy: Galaxy?): EarthMap {
    override val title get() = galaxy?.name ?: "Galaxies"
    override val layer get() = EarthLayer.Galaxy
}
data class CityMap(val city: City?): EarthMap {
    override val title get() = city?.name ?: "Cities"
    override val layer get() = EarthLayer.City
}

enum class EarthLayer {
    Galaxy,
    City,
}