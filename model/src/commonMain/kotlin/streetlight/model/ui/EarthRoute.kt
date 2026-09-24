package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.toSlug
import koala.html.SegmentParse

/** A route of the earth view, showing one of its layers. */
sealed interface EarthRoute: StreetlightRoute {
    override val screen get() = Screen.Earth
    val layer: EarthLayer
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    override val title get() = "Galaxy Map"
    override val label get() = "Galaxy"
    override val layer get() = EarthLayer.Galaxy

    override fun toRelativePath() = "/earth/galaxy/${slug?.toString() ?: ""}"
}

data class CityMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    override val title get() = "City Map"
    override val label get() = "City"
    override val layer get() = EarthLayer.City

    override fun toRelativePath() = "/earth/city/${slug?.toString() ?: ""}"
}

data class PostMapRoute(override val title: String = "Earth"): EarthRoute {
    override val layer get() = EarthLayer.Post
    override val label get() = "Post"
    override fun toRelativePath() = "/earth"
}

/** Reads an earth route from its path: `/earth`, `/earth/galaxy/{slug}`, or `/earth/city/{slug}`. */
val parseEarthRoute = SegmentParse(listOf("city", "galaxy")) { segments ->
    when (segments.getOrNull(1)) {
        "city" -> CityMapRoute(segments.takeSegment(2)?.toSlug())
        "galaxy" -> GalaxyMapRoute(segments.takeSegment(2)?.toSlug())
        else -> PostMapRoute(segments.takeSegment(2) ?: "Earth")
    }
}

/** The path segment at [index], or `null` when it is missing or blank. */
fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }