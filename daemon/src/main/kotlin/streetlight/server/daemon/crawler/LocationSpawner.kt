package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import kampfire.model.Distance
import streetlight.model.data.toOriginId
import streetlight.model.data.mergeLeft
import kampfire.model.toUrl
import kampfire.model.Outcome
import kampfire.model.Url
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
import streetlight.model.data.EventFeed
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

    /** The location of [event], read from [lead], or null when it names no place and [lead] has no location. */
    suspend fun locate(event: PropertyMap, lead: EventFeed, tracker: ParseTracker): Location? {
        val feedLocation = lead.location
        val text = event[ParseProperty.Location]?.trim()?.takeIf { it.isNotEmpty() } ?: return feedLocation
        if (feedLocation?.name?.fuzzyMatches(text) == true) return feedLocation
        val hits = search(text, lead.geoPoint) ?: return feedLocation.also { tracker.locationFellBack(event, text) }
        val place = distinctPlace(text, lead.geoPoint, hits, hasFeedLocation = feedLocation != null)
            ?: return feedLocation.also { tracker.locationFellBack(event, text) }

        dao.location.readLocationByMapId(place.osmId)?.let { return it.also { tracker.locationMatched(event, text, it) } }
        dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius)
            .firstOrNull { location -> location.name?.fuzzyMatches(text) == true }
            ?.let { return it.also { tracker.locationMatched(event, text, it) } }

        val edit = place.toEdit().takeIf { it.validity.isValid && it.state != null }
            ?: return feedLocation.also { tracker.locationFellBack(event, text) }
        val created = try {
            server.createLocation(null, edit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Problem("${e::class.simpleName}: ${e.message}")
        }.toDataOr {
            tracker.locationFailed(event, text, it)
            return feedLocation
        }
        tracker.locationSpawned(event, text, created)
        return created
    }

    /**
     * The place on the map named [name], or [declaredName] when [name] finds none, searched with [where], and the name
     * that found it: of the places whose name matches, one whose website shares the origin of [website], when known,
     * first, then the closest name.
     */
    suspend fun findPlace(
        website: Url?,
        name: String,
        declaredName: String?,
        where: String?,
    ): Pair<OSMLocation, String>? {
        findPlace(website, name, where)?.let { return it to name }
        val declared = declaredName?.takeIf { it != name } ?: return null
        return findPlace(website, declared, where)?.let { it to declared }
    }

    /** The location already stored for [place]: by its map id, or by a matching name at its point. */
    suspend fun readStored(place: OSMLocation, name: String): Location? =
        dao.location.readLocationByMapId(place.osmId)
            ?: dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius)
                .firstOrNull { location -> location.name?.fuzzyMatches(name) == true }

    /**
     * The location created for [place] from the page's [details], its [website] preferred to the map's, with the map
     * filling what the page lacks and giving the address.
     */
    suspend fun createFrom(place: OSMLocation, details: PropertyMap, website: Url?): Outcome<Location> {
        val edit = details.toLocationEdit(website).copy(address = null).mergeLeft(place.toEdit())
        if (!edit.validity.isValid || edit.state == null) return Problem("The place on the map lacks what a location needs")
        return try {
            server.createLocation(null, edit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Problem("${e::class.simpleName}: ${e.message}")
        }
    }

    /** The place on the map named [name], searched with [where], preferring one on the origin of [website]. */
    private suspend fun findPlace(website: Url?, name: String, where: String?): OSMLocation? {
        val text = listOfNotNull(name, where).joinToString(", ")
        delay(searchedAt + searchInterval - Clock.System.now())
        searchedAt = Clock.System.now()
        val hits = tryOrNull { osm.search(OSMQuery(query = text)) } ?: return null
        val origin = website?.toOriginId()
        return hits
            .filter { it.placeRank >= minPlaceRank && it.name?.fuzzyMatches(name) == true }
            .maxWithOrNull(compareBy<OSMLocation>(
                { place ->
                    val placeWebsite = place.extraTags?.website ?: place.extraTags?.contactWebsite
                    origin != null && placeWebsite?.toUrl()?.toOriginId() == origin
                },
                { place -> place.name?.similarity(name) ?: 0.0 },
            ))
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
