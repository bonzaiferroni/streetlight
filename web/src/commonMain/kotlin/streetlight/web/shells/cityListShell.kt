package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.CityListContent
import streetlight.model.ui.CityListRoute
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.feedRow
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.cityListShell(content: CityListContent) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            column(FeedSection.FeedColumnMod) {
                content.cities.forEach { city ->
                    feedRow(city, true)
                }
            }
            appFooter()
        }

        universeRouteMenu(CityListRoute)
    }

    dataIsland(CityListShell.IslandId, content)
}

object CityListShell {
    val IslandId = Id("city-list-shell__island")
}
