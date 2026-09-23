package streetlight.web.shells

import koala.SiteImage
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.CityContent
import streetlight.web.layouts.feedSection
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.entityHeader

fun FlowContent.cityShell(content: CityContent) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            entityHeader(
                entity = content.city,
                descriptor = "a city",
                image = content.city.image ?: SiteImage.PearlStreet,
            )
            feedSection(content.feed, cityId = content.city.cityId)
            appFooter()
        }
    }

    dataIsland(CityShell.IslandId, content)
}

object CityShell {
    val IslandId = Id("city-shell__island")
}
