package streetlight.web.layouts

import kampfire.api.Username
import kampfire.api.toMarkdown
import kotlinx.html.FlowContent
import streetlight.model.data.CustomEntity
import streetlight.model.data.EventEdit
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.RecordType
import kotlin.time.Clock

// previews

/** A preview of the feed row [edit] will post as, posted by [username] just now. */
fun FlowContent.postRow(edit: LocationEdit, username: Username) {
    feedRow(
        entity = CustomEntity(
            label = edit.label,
            geoPoint = edit.geoPoint,
            username = username,
            image = edit.image,
            description = edit.description,
            body = edit.description ?: edit.addressLine?.takeIf { it.isNotEmpty() }?.toMarkdown() ?: edit.label.toMarkdown(),
            links = edit.links,
            createdAt = Clock.System.now(),
            recordType = RecordType.Location,
        ),
        cells = locationCells(edit.mapType, edit.city, 0),
    )
}

/** A preview of the feed row [event] will post as at [location], posted by [username] just now. */
fun FlowContent.postRow(event: EventEdit, location: Location, username: Username) {
    feedRow(
        entity = CustomEntity(
            label = event.title ?: "[Title]",
            geoPoint = location.geoPoint,
            username = username,
            image = event.image,
            description = event.description,
            body = event.description ?: location.label.toMarkdown(),
            links = event.displayedLinks,
            createdAt = Clock.System.now(),
            recordType = RecordType.Event,
        ),
        cells = eventCells(event.startsAt, event.cost, event.website, event.tags?.firstOrNull(), location.name),
    )
}
