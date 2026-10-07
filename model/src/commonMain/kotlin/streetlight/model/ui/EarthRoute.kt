package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.toSlug
import kampfire.model.GeoPoint
import koala.html.PathParse
import koala.html.buildRelativePath
import koala.html.getAll
import streetlight.model.data.EventTag

/** A route of the earth view, showing one of its layers. */
sealed interface EarthRoute: StreetlightRoute {
    override val screen get() = Screen.Earth
    val layer: EarthLayer
}

data class GalaxyMapRoute(override val slug: Slug?): EarthRoute, SlugRoute {
    companion object {
        val nodes = listOf(EARTH_NODE, GALAXY_NODE)
    }

    override val title get() = "Galaxies"
    override val layer get() = EarthLayer.Galaxy

    // override fun toRelativePath() = "/$EARTH_NODE/$GALAXY_NODE/${slug?.toString() ?: ""}"
    override fun toRelativePath() = buildRelativePath(nodes, slug)
}

data object CityMapRoute: EarthRoute {
    val nodes = listOf(EARTH_NODE, CITIES_NODE)

    override val title get() = "Cities"
    override val layer get() = EarthLayer.City

    override fun toRelativePath() = buildRelativePath(nodes)
}

data class EventsMapRoute(
    val geoPoint: GeoPoint? = null,
    val tags: List<EventTag>? = null,
): EarthRoute {
    companion object {
        val nodes = listOf(EARTH_NODE)
    }

    override val layer get() = EarthLayer.Events
    override val title get() = "Events"
    override fun toRelativePath() = buildRelativePath(nodes) {
        geoPoint?.let {
            append(GEO_POINT_NODE, it.toString())
        }
        tags?.let {
            appendAll(TAG_PARAM, tags.map { it.name })
        }
    }
}

/** Reads an earth route from its path: `/earth`, `/earth/galaxy/{slug}`, or `/earth/cities`. */
val parseEarthRoute = PathParse(listOf(GALAXY_NODE, CITIES_NODE)) { nodes, parameters ->
    when (nodes.getOrNull(1)) {
        CITIES_NODE -> CityMapRoute
        GALAXY_NODE -> GalaxyMapRoute(nodes.takeSegment(2)?.toSlug())
        else -> {
            val tags = parameters.getAll(TAG_PARAM) { name -> EventTag.entries.firstOrNull { it.name == name } }
            EventsMapRoute(parameters[GEO_POINT_NODE]?.let { GeoPoint.of(it) } , tags)
        }
    }
}

/** The path segment at [index], or `null` when it is missing or blank. */
fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }

private const val EARTH_NODE = "earth"
private const val GALAXY_NODE = "galaxy"
private const val CITIES_NODE = "cities"

private const val TAG_PARAM = "tag"
private const val GEO_POINT_NODE = "geoPoint"