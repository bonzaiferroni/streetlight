package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.toSlug
import koala.html.SegmentParse

sealed interface EarthRoute: StreetlightRoute, SlugRoute {
    override val screen get() = Screen.Earth
    val layer: EarthLayer
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute {
    override val title get() = "Galaxy Map"
    override val label get() = "Map"
    override val layer get() = EarthLayer.Galaxy

    override fun toRelativePath() = "/earth/galaxy/${slug?.toString() ?: ""}"
}

data class CityMapRoute(override val slug: Slug?): EarthRoute {
    override val title get() = "City Map"
    override val label get() = "Map"
    override val layer get() = EarthLayer.City

    override fun toRelativePath() = "/earth/city/${slug?.toString() ?: ""}"
}

val parseEarthRoute = SegmentParse(listOf("city", "galaxy")) { segments ->
    when (segments.getOrNull(1)) {
        "city" -> CityMapRoute(segments.takeSegment(2)?.toSlug())
        else -> GalaxyMapRoute(segments.takeSegment(2)?.toSlug())
    }
}

fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }