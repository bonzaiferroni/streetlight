package streetlight.web.layouts

import kabinet.utils.toMetricString
import kampfire.api.Username
import koala.SvgFile
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit

fun cellsOf(location: Location) = listOfNotNull(
    EntityCell(SvgFile.MapPin, location.mapType ?: "Location", null),
    location.city?.let { EntityCell(SvgFile.City, it, null) },
)

fun cellsOf(username: Username?, edit: LocationEdit) = listOf(
    starCell(username),
)

fun cellsOf(event: EventEdit) = listOfNotNull(
    startsAtCell(event.startsAt),
    event.cost?.let { costCell(it, event.website) },
)

fun cellsOf(event: EventLocation) = listOfNotNull(
    event.startsAt?.let { dateCell(it) },
    startsAtCell(event.startsAt),
    event.cost?.let { costCell(it, event.url) },
    event.locationName?.let { EntityCell(SvgFile.MapPin, it, null) },
)

fun cellsOf(event: Event) = listOfNotNull(
    event.startsAt?.let { dateCell(it) },
    startsAtCell(event.startsAt),
    event.cost?.let { costCell(it, event.website) },
)

fun cellsOf(galaxy: Galaxy) = listOf(
    EntityCell(SvgFile.Calendar, galaxy.eventCount.toMetricString(), null),
)
