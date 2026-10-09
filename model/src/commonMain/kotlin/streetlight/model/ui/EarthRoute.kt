package streetlight.model.ui

import kampfire.api.Slug
import kampfire.api.toSlug
import kampfire.model.GeoRect
import koala.html.PathParse
import koala.html.buildRelativePath
import streetlight.model.data.EventTag

/** A route of the earth view, showing one of its layers. */
sealed interface EarthRoute: StreetlightRoute {
    val layer: EarthLayer
    override val screen get() = Screen.Earth
    val bounds: GeoRect? get() = null
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
    override val bounds: GeoRect? = null,
    val tag: EventTag? = null,
): EarthRoute {
    companion object {
        val nodes = listOf(EARTH_NODE)
    }

    override val layer get() = EarthLayer.Events
    override val title get() = "Events"
    override fun toRelativePath() = buildRelativePath(nodes) {
        bounds?.let {
            append(RECT_NODE, it.toString())
        }
        tag?.let {
            append(TAG_PARAM, tag.name)
        }
    }
}

/** Reads an earth route from its path: `/earth`, `/earth/galaxy/{slug}`, or `/earth/cities`. */
val parseEarthRoute = PathParse(listOf(GALAXY_NODE, CITIES_NODE)) { nodes, parameters ->
    when (nodes.getOrNull(1)) {
        CITIES_NODE -> CityMapRoute
        GALAXY_NODE -> GalaxyMapRoute(nodes.takeSegment(2)?.toSlug())
        else -> {
            val tag = parameters[TAG_PARAM]?.let { EventTag.valueOf(it) }
            val rect = parameters[RECT_NODE]?.let { GeoRect.of(it) }
            EventsMapRoute(rect, tag)
        }
    }
}

/** The path segment at [index], or `null` when it is missing or blank. */
fun List<String>.takeSegment(index: Int) = getOrNull(index)?.takeIf { it.isNotBlank() }

private const val EARTH_NODE = "earth"
private const val GALAXY_NODE = "galaxy"
private const val CITIES_NODE = "cities"

private const val TAG_PARAM = "tag"
private const val RECT_NODE = "rect"