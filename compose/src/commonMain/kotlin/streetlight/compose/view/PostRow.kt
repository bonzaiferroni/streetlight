package streetlight.compose.view

import androidx.compose.runtime.Composable
import kampfire.api.Username
import kampfire.api.toMarkdown
import streetlight.model.data.CustomEntity
import streetlight.model.data.EventEdit
import streetlight.model.data.FeedContext
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.RecordType
import streetlight.model.data.eventDetailsOf
import streetlight.model.data.locationDetailsOf
import kotlin.time.Clock

/**
 * A preview of the feed row [edit] will post as, laid out by [mode], posted by [username] just now. The cousin of the
 * web's `postRow`.
 */
@Composable
fun PostRow(edit: LocationEdit, username: Username, mode: FeedMode) {
    val entity = CustomEntity(
        label = edit.label,
        geoPoint = edit.geoPoint,
        username = username,
        image = edit.image,
        description = edit.description,
        body = edit.description ?: edit.addressLine?.takeIf { it.isNotEmpty() }?.toMarkdown() ?: edit.label.toMarkdown(),
        links = edit.links,
        createdAt = Clock.System.now(),
        recordType = RecordType.Location,
    )
    context(null as FeedContext?) { FeedRow(entity, mode, details = locationDetailsOf(edit.mapType, edit.city, 0)) }
}

/**
 * A preview of the feed row [event] will post as at [location], laid out by [mode], posted by [username] just now. The
 * cousin of the web's `postRow`.
 */
@Composable
fun PostRow(event: EventEdit, location: Location, username: Username, mode: FeedMode) {
    val entity = CustomEntity(
        label = event.title ?: "[Title]",
        geoPoint = location.geoPoint,
        username = username,
        image = event.image,
        description = event.description,
        body = event.description ?: location.label.toMarkdown(),
        links = event.displayedLinks,
        createdAt = Clock.System.now(),
        recordType = RecordType.Event,
    )
    val details = eventDetailsOf(event.startsAt, event.cost, event.website, event.tags.firstOrNull(), location.name)
    context(null as FeedContext?) { FeedRow(entity, mode, details = details) }
}
