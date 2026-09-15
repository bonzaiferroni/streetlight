package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.toSlug
import koala.html.SegmentParse

sealed interface EarthRoute: StreetlightRoute {
    override val screen get() = Screen.Earth
    override val label get() = "Map"
    val layer: EarthLayer
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    override val title get() = "Galaxy Map"
    override val layer get() = EarthLayer.Galaxy

    override fun toRelativePath() = "/earth/galaxy/${slug?.toString() ?: ""}"
}

data class CityMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    override val title get() = "City Map"
    override val layer get() = EarthLayer.City

    override fun toRelativePath() = "/earth/city/${slug?.toString() ?: ""}"
}

data class PostMapRoute(override val title: String): EarthRoute {
    override val layer get() = EarthLayer.Post

    override fun toRelativePath() = "/earth"
}

val parseEarthRoute = SegmentParse(listOf("city", "galaxy")) { segments ->
    when (segments.getOrNull(1)) {
        "city" -> CityMapRoute(segments.takeSegment(2)?.toSlug())
        "galaxy" -> GalaxyMapRoute(segments.takeSegment(2)?.toSlug())
        else -> PostMapRoute(segments.takeSegment(2) ?: "Earth")
    }
}

fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }