package streetlight.web.shell

import koala.component.column
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.renderLayout
import koala.page.*

/** The home of a LocationApp: the location's layout, with no app header or footer. */
fun FlowContent.locationAppShell(content: LocationContent) {
    column(BodyStyle.ShellColumn) {
        renderLayout(content)
    }

    dataIsland(LocationAppShell.IslandId, content)
}

object LocationAppShell {
    val IslandId = Id("location-app-shell__island")
}
