package streetlight.web.layouts

import kampfire.api.Username
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
            body = edit.description,
            links = edit.links,
            createdAt = Clock.System.now(),
            recordType = RecordType.Location,
        ),
        isUniverse = false,
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
            body = event.description,
            links = event.displayedLinks,
            createdAt = Clock.System.now(),
            recordType = RecordType.Event,
        ),
        isUniverse = false,
        cells = eventCells(event.startsAt, event.cost, event.website, location.name),
    )
}
