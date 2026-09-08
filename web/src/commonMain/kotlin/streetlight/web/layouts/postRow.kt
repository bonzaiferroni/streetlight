package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.api.Username
import koala.Image
import koala.html.AppRoute
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.CustomEntity
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.ExtraLink
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Post
import streetlight.model.data.FeedEntity
import streetlight.model.data.FeedMark
import streetlight.model.data.PostMark
import streetlight.model.data.PostType

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