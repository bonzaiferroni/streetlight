package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.enumParamOf
import kampfire.api.geoRectParamOf
import kampfire.api.read
import kampfire.api.stringParamOf
import kampfire.api.toSlug
import kampfire.model.GeoRect
import koala.model.PathParse
import koala.model.buildRelativePath
import streetlight.model.data.EventTag

/** A route of the earth view, showing one of its layers. */
sealed interface EarthRoute: StreetlightRoute {
    val layer: EarthLayer
    override val screen get() = Screen.Earth
    // the earth view handles each of its routes itself
    override val screenKey: Any get() = Screen.Earth
    val bounds: GeoRect? get() = null
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    companion object {
        val nodes = listOf(GALAXY_NODE)
    }

    override val title get() = "Galaxies"
    override val layer get() = EarthLayer.Galaxy

    // override fun toRelativePath() = "/$EARTH_NODE/$GALAXY_NODE/${slug?.toString() ?: ""}"
    override fun toRelativePath() = buildRelativePath(nodes, slug)
}

data object CityMapRoute: EarthRoute {
    val nodes = listOf(CITIES_NODE)

    override val title get() = "Cities"
    override val layer get() = EarthLayer.City

    override fun toRelativePath() = buildRelativePath(nodes)
}

data class EventsMapRoute(
    override val bounds: GeoRect? = null,
    val tag: EventTag? = null,
    val searchText: String? = null,
): EarthRoute {
    companion object {
        val search = stringParamOf("search")
        val tag = enumParamOf<EventTag>("tag")
        val bounds = geoRectParamOf("rect")
    }

    override val layer get() = EarthLayer.Events
    override val title get() = "Events"
    override fun toRelativePath() = buildRelativePath {
        writeParam(Companion.bounds, bounds)
        writeParam(Companion.tag, tag)
        writeParam(search, searchText)
    }
}

/** Reads an earth route from its path: `/earth`, `/earth/galaxy/{slug}`, or `/earth/cities`. */
val parseEarthRoute = PathParse(listOf(GALAXY_NODE, CITIES_NODE)) { nodes ->
    when (nodes.getOrNull(1)) {
        CITIES_NODE -> CityMapRoute
        GALAXY_NODE -> GalaxyMapRoute(nodes.takeSegment(2)?.toSlug())
        else -> {
            val tag = read(EventsMapRoute.tag)
            val rect = read(EventsMapRoute.bounds)
            val searchText = read(EventsMapRoute.search)?.trim()?.takeIf { it.isNotEmpty() }
            EventsMapRoute(rect, tag, searchText)
        }
    }
}

/** The path segment at [index], or `null` when it is missing or blank. */
fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }

private const val GALAXY_NODE = "galaxy"
private const val CITIES_NODE = "cities"