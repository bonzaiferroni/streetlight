package streetlight.web.shell

import kotlinx.html.FlowContent
import streetlight.model.data.CityListContent
import streetlight.web.layout.FeedSection
import streetlight.web.layout.feedRow
import streetlight.web.layout.mainBody
import koala.layout.*
import koala.page.*

fun FlowContent.cityListShell(content: CityListContent) {
    mainBody("cityListShell.kt") {
        column(FeedSection.FeedColumnMod) {
            content.cities.forEach { city ->
                feedRow(city)
            }
        }
    }

    dataIsland(CityListShell.IslandId, content)
}

object CityListShell {
    val IslandId = Id("city-list-shell__island")
}
