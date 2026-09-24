package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.CityListContent
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.feedRow
import streetlight.web.ui.mainBody

fun FlowContent.cityListShell(content: CityListContent) {
    mainBody("cityListShell.kt") {
        column(FeedSection.FeedColumnMod) {
            content.cities.forEach { city ->
                feedRow(city, true)
            }
        }
    }

    dataIsland(CityListShell.IslandId, content)
}

object CityListShell {
    val IslandId = Id("city-list-shell__island")
}
