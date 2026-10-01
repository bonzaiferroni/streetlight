package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import kampfire.model.Distance
import streetlight.model.data.toOriginId
import streetlight.model.data.mergeLeft
import kampfire.model.toUrl
import kampfire.model.Ok
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.EventFeed
import streetlight.model.data.toEdit
import streetlight.model.external.OSMLocation
import streetlight.model.external.OSMQuery
import streetlight.model.external.toGeoPoint
import streetlight.server.db.datascope.createLocation
import streetlight.server.model.MapReferenceClient
import streetlight.server.model.Server
import kotlin.math.cos

/**
 * Finds the location of an event: for an event of a location's feed, the distinct place its location text names, or
 * else the feed's own location; for any other, the place its name and address find, or the address alone. Places are
 * read when stored and created when not. Its map searches wait on [gate].
 */
class LocationSpawner(
    private val server: Server,
    private val osm: MapReferenceClient,
    private val gate: OSMGate,
) {
    private val dao get() = server.dao
    private val placesMutex = Mutex()
    private val places = mutableMapOf<Pair<String, GeoPoint>, List<OSMLocation>>()

    /**
     * The location of [event], read from [lead], or null when it names no place and [lead] has no location. An event
     * of a feed with no location of its own that gives an address is placed by [placeEvent], near the feed.
     */
    context(crawler: Crawler)
    suspend fun locateEvent(event: PropertyMap, lead: EventFeed, tracker: ParseTracker): Location? {
        val address = event[ParseProperty.Address]?.trim()?.takeIf { it.isNotEmpty() }
        return when {
            lead.location == null && address != null -> {
                val name = event[ParseProperty.Location]?.trim()?.takeIf { it.isNotEmpty() } ?: address
                val area = event[ParseProperty.Area]
                placeEvent(name, address, area, event[ParseProperty.Region], null, lead.geoPoint, event, tracker)
            }
            else -> locateDistinctPlace(event, lead, tracker)
        }
    }

    /**
     * The location of the distinct place the location text of [event] names near the feed of [lead], or else the
     * feed's own location.
     */
    context(crawler: Crawler)
    private suspend fun locateDistinctPlace(event: PropertyMap, lead: EventFeed, tracker: ParseTracker): Location? {
        val feedLocation = lead.location
        val text = event[ParseProperty.Location]?.trim()?.takeIf { it.isNotEmpty() }?.let(::cleanPlaceName) ?: return feedLocation
        if (feedLocation?.name?.fuzzyMatches(text) == true) return feedLocation
        val foundPlaces = searchPlaces(text, lead.geoPoint) ?: return feedLocation.also { tracker.trackFallbackLocation(event, text) }
        val place = distinctPlace(text, lead.geoPoint, foundPlaces, hasFeedLocation = feedLocation != null)
            ?: return feedLocation.also { tracker.trackFallbackLocation(event, text) }

        val location = crawler.dbWrite {
            dao.location.readLocationByMapId(place.osmId)?.let { return it.also { tracker.trackMatchedLocation(event, text, it) } }
            dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius)
                .firstOrNull { location -> location.name?.fuzzyMatches(text) == true }
                ?.let { return it.also { tracker.trackMatchedLocation(event, text, it) } }

            val edit = place.toEdit().takeIf { it.validity.isValid }
                ?: return feedLocation.also { tracker.trackFallbackLocation(event, text) }
            try {
                server.createLocation(null, edit, isImageRequired = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Problem("${e::class.simpleName}: ${e.message}")
            }
        }.toDataOr {
            tracker.trackFailedLocation(event, text, it)
            return feedLocation
        }
        tracker.trackSpawnedLocation(event, text, location)
        return location
    }

    /**
     * The place on the map at [address] in [area] named [name], and the name that found it. The address alone is
     * searched first, then the name with the address, then [declaredName] with it when [name] finds none. Then [name]
     * with its [region] alone, and with nothing else when [near] fences it. Of the places whose name matches, one whose
     * website shares the origin of [website], when known, wins, then one on the street of [address], then the closest
     * name. A place farther than [searchRadius] from [near], when given, is not taken.
     */
    suspend fun findPlace(
        website: Url?,
        name: String,
        declaredName: String?,
        address: String?,
        area: String?,
        region: String?,
        near: GeoPoint? = null,
    ): Pair<OSMLocation, String>? {
        val street = address?.let { withoutUnit(it).substringBefore(',').trim() }
        val where = listOfNotNull(street, area).joinToString(", ").ifEmpty { null }
        if (street != null) {
            searchPlace(website, listOfNotNull(street, area).joinToString(", "), name, street, near)?.let { return it to name }
        }
        val declared = declaredName?.takeIf { it != name }
        val searches = listOfNotNull(
            listOfNotNull(name, where) to name,
            declared?.let { listOfNotNull(it, where) to it },
            region?.let { listOf(name, it) to name },
            near?.let { listOf(name) to name },
        )
        return searches.firstNotNullOfOrNull { (parts, searchedName) ->
            searchPlace(website, parts.joinToString(", "), searchedName, street, near)?.let { it to searchedName }
        }
    }

    /** The location already stored at [address] whose name matches [name], found without the map. */
    suspend fun readLocationAt(name: String, address: String?): Location? =
        address?.let { dao.location.readLocationAt(null, it) }?.takeIf { it.name?.fuzzyMatches(name) == true }

    /** The location already stored for [place]: by its map id, or by a matching name at its point. */
    suspend fun readStoredLocation(place: OSMLocation, name: String): Location? =
        dao.location.readLocationByMapId(place.osmId)
            ?: dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius)
                .firstOrNull { location -> location.name?.fuzzyMatches(name) == true }

    /**
     * The location created for [place] from the page's [details], its [website] preferred to the map's, with the map
     * filling what the page lacks and giving the address.
     */
    context(crawler: Crawler)
    suspend fun createLocationFrom(place: OSMLocation, details: PropertyMap, website: Url?): Outcome<Location> {
        val edit = details.toLocationEdit(website).copy(address = null).mergeLeft(place.toEdit())
        return create(edit.withUnitOf(details[ParseProperty.Address]))
    }

    /**
     * The location created at the address of [place] from the page's [details], as [createLocationFrom] creates it but with no
     * map id, since the map names something else there or nothing.
     */
    context(crawler: Crawler)
    suspend fun createNamedLocationAt(place: OSMLocation, details: PropertyMap, website: Url?): Outcome<Location> {
        val edit = details.toLocationEdit(website).copy(address = null).mergeLeft(place.toAddressEdit())
        return create(edit.withUnitOf(details[ParseProperty.Address]))
    }

    /**
     * The location of the place named [givenName] at [address] in [area] or [region], for [event], its name cleaned by
     * [cleanPlaceName]: one stored at the address under its name; else the place its address and name find on the
     * map, stored or created, its [website] preferred to the map's; else the address alone, stored or created with no
     * name of its own. A place farther than [searchRadius] from [near], when given, is not taken. Null when none is
     * found or created.
     */
    context(crawler: Crawler)
    suspend fun placeEvent(
        givenName: String,
        address: String?,
        area: String?,
        region: String?,
        website: Url?,
        near: GeoPoint?,
        event: PropertyMap,
        tracker: ParseTracker,
    ): Location? {
        val name = cleanPlaceName(givenName)
        readLocationAt(name, address)?.let {
            tracker.trackMatchedLocation(event, name, it)
            return it
        }
        return when (val found = findPlace(website, name, null, address, area, region, near)) {
            null -> address?.let { placeAtAddress(it, area, region, website, near, event, name, tracker) }
            else -> readOrCreateLocation(found.first, found.second, website, event, name, tracker)
        }
    }

    /**
     * The location of [place], found on the map by [placedName], for [event] whose place is named [name]: stored
     * already, or created from the map with [website] preferred to the map's.
     */
    context(crawler: Crawler)
    private suspend fun readOrCreateLocation(
        place: OSMLocation,
        placedName: String,
        website: Url?,
        event: PropertyMap,
        name: String,
        tracker: ParseTracker,
    ): Location? {
        readStoredLocation(place, placedName)?.let {
            tracker.trackMatchedLocation(event, name, it)
            return it
        }
        val location = createLocationFrom(place, mapOf(ParseProperty.Name to placedName), website).toDataOr {
            tracker.trackFailedLocation(event, name, it)
            return null
        }
        tracker.trackSpawnedLocation(event, name, location)
        return location
    }

    /**
     * The location at the [address] in [area] or [region] of an event whose place, named [name], the map does not know: stored
     * there already, or created there with no name of its own.
     */
    context(crawler: Crawler)
    private suspend fun placeAtAddress(
        address: String,
        area: String?,
        region: String?,
        website: Url?,
        near: GeoPoint?,
        event: PropertyMap,
        name: String,
        tracker: ParseTracker,
    ): Location? {
        val place = findAddress(address, area, region, near) ?: return null
        readUnnamedLocation(place)?.let {
            tracker.trackMatchedLocation(event, name, it)
            return it
        }
        val location = createLocationAt(place, website).toDataOr {
            tracker.trackFailedLocation(event, name, it)
            return null
        }
        tracker.trackSpawnedLocation(event, name, location)
        return location
    }

    /**
     * The place on the map at [address] in [area], or in [region] alone when [area] finds none: a building-level hit
     * with the address's house number on a road that holds the longest word of its street, so that "Welton St" finds
     * "Welton Street"; one without a name of its own first. A place farther than [searchRadius] from [near], when
     * given, is not taken.
     */
    suspend fun findAddress(address: String, area: String?, region: String?, near: GeoPoint? = null): OSMLocation? {
        val street = withoutUnit(address).substringBefore(',').trim()
        val number = houseNumberPattern.find(street)?.value ?: return null
        val searches = listOfNotNull(listOfNotNull(street, area), region?.let { listOf(street, it) }).distinct()
        return searches.firstNotNullOfOrNull { parts ->
            gate.waitUntilOpen()
            val foundPlaces = tryOrNull { osm.search(OSMQuery(query = parts.joinToString(", "))) } ?: return@firstNotNullOfOrNull null
            foundPlaces
                .filter { it.address.number == number && it.isNear(near) && it.isOnStreet(street) }
                .minByOrNull { if (it.name.isNullOrBlank()) 0 else 1 }
        }
    }

    /** The location already stored at the point of [place] with no name of its own. */
    suspend fun readUnnamedLocation(place: OSMLocation): Location? =
        dao.location.readNearbyLocations(place.toGeoPoint(), sameVenueRadius).firstOrNull { it.name.isNullOrBlank() }

    /**
     * The location created at the address of [place], with no name and no map id, since it is the address and not
     * the thing the map names there; [website] when known.
     */
    context(crawler: Crawler)
    suspend fun createLocationAt(place: OSMLocation, website: Url?): Outcome<Location> =
        create(place.toAddressEdit().copy(website = website))

    /**
     * The location created from [edit], when it has a point. A location already stored under its map id is returned
     * in its place.
     */
    context(crawler: Crawler)
    private suspend fun create(edit: LocationEdit): Outcome<Location> {
        if (edit.geoPoint == null) {
            return Problem("The place on the map has no point")
        }
        return crawler.dbWrite {
            edit.mapId?.let { dao.location.readLocationByMapId(it) }?.let { return@dbWrite Ok(it) }
            try {
                server.createLocation(null, edit, isImageRequired = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Problem("${e::class.simpleName}: ${e.message}")
            }
        }
    }

    /**
     * The place on the map found by the search [text] and named [name], within [searchRadius] of [near] when given,
     * preferring one on the origin of [website], then one on [street].
     */
    private suspend fun searchPlace(website: Url?, text: String, name: String, street: String?, near: GeoPoint?): OSMLocation? {
        gate.waitUntilOpen()
        val foundPlaces = tryOrNull { osm.search(OSMQuery(query = text)) } ?: return null
        val origin = website?.toOriginId()
        return foundPlaces
            .filter { it.isSpot && it.name?.fuzzyMatches(name) == true && it.isNear(near) }
            .maxWithOrNull(compareBy<OSMLocation>(
                { place ->
                    val placeWebsite = place.extraTags?.website ?: place.extraTags?.contactWebsite
                    origin != null && placeWebsite?.toUrl()?.toOriginId() == origin
                },
                { place -> street != null && place.isOnStreet(street) },
                { place -> place.name?.similarity(name) ?: 0.0 },
            ))
    }

    /** The map places named [text] within [searchRadius] of [point], or null when the search failed. */
    private suspend fun searchPlaces(text: String, point: GeoPoint): List<OSMLocation>? {
        val key = text.lowercase() to point
        placesMutex.withLock { places[key] }?.let { return it }
        gate.waitUntilOpen()
        val foundPlaces = tryOrNull {
            osm.search(OSMQuery(amenity = text, bounds = point.boundsWithin(searchRadius)))
        } ?: return null
        placesMutex.withLock { places[key] = foundPlaces }
        return foundPlaces
    }
}

/**
 * The place among [foundPlaces] that [text] names within [searchRadius] of the feed at [feedPoint], the closest name first
 * and then the nearest, unless [hasFeedLocation] and a place it names lies at the feed's location.
 */
fun distinctPlace(text: String, feedPoint: GeoPoint, foundPlaces: List<OSMLocation>, hasFeedLocation: Boolean = true): OSMLocation? {
    val named = foundPlaces.filter { place ->
        place.isSpot && place.name?.fuzzyMatches(text) == true &&
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

/**
 * Whether this place is somewhere a person can go, such as a business, building or park, and not an area or a road:
 * a city, town or neighborhood, a street, or an administrative boundary.
 */
private val OSMLocation.isSpot get() =
    category !in areaCategories && !(category == "boundary" && type == "administrative")

/**
 * This name of a place without its notes in parentheses or brackets and without phrases that name no place, such as
 * "parking lot"; the name as given when nothing is left.
 */
internal fun cleanPlaceName(name: String): String = placePhrases
    .fold(name.replace(placeNote, " ")) { cleaned, phrase -> cleaned.replace(phrase, " ") }
    .replace(whitespaceRun, " ").trim().trim('-', ',', ' ')
    .ifEmpty { name }

/**
 * Whether this place is on the road of [street]: its road holds the longest word of [street] after the house number,
 * so that "Welton St" is on "Welton Street".
 */
private fun OSMLocation.isOnStreet(street: String): Boolean {
    val number = houseNumberPattern.find(street)?.value
    val roadName = (number?.let { street.replaceFirst(it, "") } ?: street).split(' ').maxByOrNull { it.length }
        ?.takeIf { it.isNotEmpty() } ?: return false
    return address.road?.contains(roadName, ignoreCase = true) == true
}

/** Whether this place lies within [searchRadius] of [point], or [point] is not given. */
private fun OSMLocation.isNear(point: GeoPoint?) = point == null || toGeoPoint().distanceTo(point) <= searchRadius

/** The edit of this place's address alone, carrying nothing of the business the map names there. */
private fun OSMLocation.toAddressEdit() = toEdit().let {
    LocationEdit(address = it.address, city = it.city, state = it.state, country = it.country, geoPoint = it.geoPoint)
}

/** This address without the unit it names, such as "#100", "Unit 148" or "Ste 1400", which the map does not know. */
internal fun withoutUnit(address: String): String = address.replace(addressUnit, "").trim().trimEnd(',').trim()

/** This edit with the unit [address] names added to its own address. */
private fun LocationEdit.withUnitOf(address: String?): LocationEdit {
    val unit = address?.let { addressUnit.find(it) }?.value?.trim()?.trimStart(',')?.trim() ?: return this
    return copy(address = this.address?.let { "$it $unit" })
}

internal val searchRadius = 200.kilometers
internal val sameVenueRadius = 150.meters
private val houseNumberPattern = Regex("""\b\d+[A-Za-z]?\b""")
private val addressUnit = Regex(""",?\s*(?:#\s*|\b(?:unit|suite|ste|apt|apartment|room|rm|building|bldg)\b\.?\s*#?\s*)(?:[A-Za-z]?\d[A-Za-z0-9-]*|[A-Za-z])\b""", RegexOption.IGNORE_CASE)
private val areaCategories = setOf("place", "highway")
private val placeNote = Regex("""\([^)]*\)|\[[^\]]*]""")
private val placePhrases = listOf(Regex("""\bparking lot\b""", RegexOption.IGNORE_CASE))
private val whitespaceRun = Regex("""\s+""")
private const val kilometersPerDegree = 111.32
