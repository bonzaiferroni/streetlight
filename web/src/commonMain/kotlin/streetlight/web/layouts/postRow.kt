package streetlight.web.layouts

import kampfire.api.Username
import kotlinx.html.FlowContent
import streetlight.model.data.CustomEntity
import streetlight.model.data.EventEdit
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit

// previews

fun FlowContent.postRow(edit: LocationEdit, username: Username?) {
    feedRow(
        entity = CustomEntity(
            label = edit.label,
            username = username,
            image = edit.image,
            body = edit.description,
            links = edit.links,
        ),
        isUniverse = false
    )
}

fun FlowContent.postRow(event: EventEdit, location: Location) {
    feedRow(
        entity = CustomEntity(
            label = event.title ?: "[Title]",
            username = null,
            image = event.image,
            body = event.description,
            links = event.displayedLinks,
        ),
        isUniverse = false
    ) {
        cellContentOf(event)()
    }
}