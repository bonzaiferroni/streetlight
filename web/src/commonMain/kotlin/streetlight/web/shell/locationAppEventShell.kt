package streetlight.web.shell

import koala.component.column
import kotlinx.html.FlowContent
import koala.component.section
import streetlight.model.data.EventLocation
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.eventBody
import koala.page.*

/** An event of a LocationApp, with no app header or footer. */
fun FlowContent.locationAppEventShell(event: EventLocation) {
    column(BodyStyle.ShellColumn) {
        section(BodyStyle.MainColumn) {
            eventBody(event)
        }
    }

    dataIsland(LocationAppEventShell.IslandId, event)
}

object LocationAppEventShell {
    val IslandId = Id("location-app-event-shell__island")
}
