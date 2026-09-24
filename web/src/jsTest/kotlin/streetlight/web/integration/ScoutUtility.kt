package streetlight.web.integration

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.EventStatus
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import kotlin.time.Clock

val DENVER = GeoPoint(-104.99, 39.74)

/** A stored location in Denver named [name]. */
fun testLocation(name: String = "The Fox Den"): Location {
    val now = Clock.System.now()
    return Location(
        locationId = LocationId.random(),
        cityId = null,
        mapId = null,
        timezoneId = "America/Denver",
        slug = Slug("the-fox-den"),
        scout = null,
        host = null,
        name = name,
        description = null,
        address = null,
        city = "Denver",
        state = "Colorado",
        geoPoint = DENVER,
        mapRank = null,
        mapCategory = null,
        mapType = null,
        resources = emptySet(),
        hours = null,
        website = null,
        starCount = null,
        eventCount = 0,
        isLit = false,
        eventsUrl = null,
        image = null,
        extraLinks = null,
        updatedAt = now,
        createdAt = now,
    )
}

/** A stored event titled [title] at [location]. */
fun testEventLocation(location: Location, title: String = "Open Mic"): EventLocation {
    val now = Clock.System.now()
    return EventLocation(
        eventId = EventId.random(),
        locationId = location.locationId,
        eventSlug = Slug("open-mic"),
        locationSlug = location.slug,
        host = Username("alice"),
        url = null,
        eventImage = null,
        title = title,
        description = null,
        cost = null,
        status = EventStatus.Live,
        visibility = 0,
        geoPoint = location.geoPoint,
        eventLinks = null,
        locationName = location.name,
        locationDescription = null,
        address = null,
        city = location.city,
        locationImage = null,
        lightCount = null,
        isLit = false,
        startsAt = null,
        endsAt = null,
        updatedAt = now,
        createdAt = now,
    )
}
