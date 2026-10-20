package streetlight.web.shell

import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.web.layout.eventBody
import streetlight.web.layout.mainBody
import koala.page.*

fun FlowContent.eventShell(event: EventLocation) {
    mainBody("eventShell.kt") {
        eventBody(event)
    }

    dataIsland(EventShell.island, event)
}

object EventShell {
    val island = Id("event-shell-island")
}
