package streetlight.web.shells

import kotlinx.html.FlowContent
import streetlight.model.data.CityListContent
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.feedRow
import streetlight.web.ui.mainBody
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
