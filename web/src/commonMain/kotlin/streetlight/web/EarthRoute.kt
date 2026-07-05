package streetlight.web

import kampfire.api.Slug
import kampfire.api.toSlug
import koala.html.AppRoute
import koala.html.SegmentParse
import streetlight.model.data.CityId

sealed interface EarthRoute: AppRoute {
    override val screen get() = StreetlightScreen.Earth
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    override val title get() = "Galaxy Map"
    override val label get() = "Map"

    override fun toSitePath() = "/earth/galaxy/${slug?.toString() ?: ""}"
}

data class CityMapRoute(val cityId: CityId?): EarthRoute, StreetlightRoute {
    override val title get() = "City Map"
    override val label get() = "Map"

    override fun toSitePath() = "/earth/city/${cityId?.toString() ?: ""}"
}

val parseEarthRoute = SegmentParse(listOf("city", "galaxy")) { segments ->
    when (segments.getOrNull(1)) {
        "city" -> CityMapRoute(segments.takeSegment(2)?.toIntOrNull()?.let { CityId(it) })
        else -> GalaxyMapRoute(segments.takeSegment(2)?.toSlug())
    }
}

fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }