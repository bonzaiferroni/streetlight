package streetlight.server.daemon

import kampfire.model.Distance
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.distanceTo
import kampfire.model.kilometers
import kampfire.model.meters
import kampfire.model.Problem
import kampfire.model.toDataOr
import kampfire.utils.fuzzyMatches
import kampfire.utils.similarity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import streetlight.model.data.Location
import streetlight.model.data.EventFeedSource
import streetlight.model.data.toEdit
import streetlight.model.external.OSMLocation
import streetlight.model.external.OSMQuery
import streetlight.model.external.toGeoPoint
import streetlight.server.db.datascope.createLocation
import streetlight.server.model.MapReferenceClient
import streetlight.server.model.Server
import kotlin.math.cos
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Finds the location of an event read from a feed: the distinct place its location text names, read or created, or
 * else the feed's own location, when it has one.
 */
class LocationSpawner(
    private val server: Server,
    private val osm: MapReferenceClient,
) {
    private val dao get() = server.dao
    private val places = mutableMapOf<Pair<String, GeoPoint>, List<OSMLocation>>()
    private var searchedAt = Instant.DISTANT_PAST

    /** The location of [event], read from [source], or null when it names no place and [source] has no location. */
    suspend fun locate(event: RawEvent, source: EventFeedSource, tracker: ParseTracker): Location? {
        val feedLocation = source.location
        val text = event.location?.trim()?.takeIf { it.isNotEmpty() } ?: return feedLocation
        if (feedLocation?.name?.fuzzyMatches(text) == true) return feedLocation
        val hits = search(text, source.geoPoint) ?: return feedLocation.also { tracker.locationFellBack(text) }
        val place = distinctPlace(text, source.geoPoint, hits, hasFeedLocation = feedLocation != null)
            ?: return feedLocation.also { tracker.locationFellBack(text) }

        dao.location.readLocationByMapId(place.osmId)?.let { return it.also { tracker.locationMatched(text, it) } }
        dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius)
            .firstOrNull { location -> location.name?.fuzzyMatches(text) == true }
            ?.let { return it.also { tracker.locationMatched(text, it) } }

        val edit = place.toEdit().takeIf { it.validity.isValid && it.state != null }
            ?: return feedLocation.also { tracker.locationFellBack(text) }
        val created = try {
            server.createLocation(null, edit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Problem("${e::class.simpleName}: ${e.message}")
        }.toDataOr {
            tracker.locationFailed(text, it)
            return feedLocation
        }
        tracker.locationSpawned(text, created)
        return created
    }

    /** The map places named [text] within [searchRadius] of [point], or null when the search failed. */
    private suspend fun search(text: String, point: GeoPoint): List<OSMLocation>? {
        val key = text.lowercase() to point
        places[key]?.let { return it }
        delay(searchedAt + searchInterval - Clock.System.now())
        searchedAt = Clock.System.now()
        val hits = tryOrNull {
            osm.search(OSMQuery(amenity = text, bounds = point.boundsWithin(searchRadius)))
        } ?: return null
        places[key] = hits
        return hits
    }
}

/**
 * The place among [hits] that [text] names within [searchRadius] of the feed at [feedPoint], the closest name first
 * and then the nearest, unless [hasFeedLocation] and a place it names lies at the feed's location.
 */
fun distinctPlace(text: String, feedPoint: GeoPoint, hits: List<OSMLocation>, hasFeedLocation: Boolean = true): OSMLocation? {
    val named = hits.filter { place ->
        place.placeRank >= minPlaceRank && place.name?.fuzzyMatches(text) == true &&
            place.toGeoPoint().distanceTo(feedPoint) <= searchRadius
    }
    if (hasFeedLocation && named.any { it.toGeoPoint().distanceTo(feedPoint) < sameVenueRadius }) return null
    return named.maxWithOrNull(
        compareBy<OSMLocation> { it.name?.similarity(text) ?: 0.0 }
            .thenByDescending { it.toGeoPoint().distanceTo(feedPoint) }
    )
}

/** The result of [block], or null when it throws. */
private suspend fun <T> tryOrNull(block: suspend () -> T?): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

/** The bounds reaching [distance] from this point in each direction. */
private fun GeoPoint.boundsWithin(distance: Distance): GeoRect {
    val latSpan = distance.inKilometers() / kilometersPerDegree
    val lngSpan = latSpan / cos(Math.toRadians(lat))
    return GeoRect(
        sw = GeoPoint(lng = lng - lngSpan, lat = lat - latSpan),
        ne = GeoPoint(lng = lng + lngSpan, lat = lat + latSpan),
    )
}

internal val searchRadius = 200.kilometers
internal val sameVenueRadius = 150.meters
private val searchInterval = 1.seconds
private const val minPlaceRank = 30
private const val kilometersPerDegree = 111.32
