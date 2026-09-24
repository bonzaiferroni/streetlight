package streetlight.model.ui

import streetlight.model.data.City
import streetlight.model.data.Galaxy

/** What the earth view shows: posts, galaxies, or cities, with its title. */
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
data class PostMap(override val title: String): EarthMap {
    override val layer get() = EarthLayer.Post
}

/** The layers of the earth view. */
enum class EarthLayer {
    Post,
    Galaxy,
    City,
}