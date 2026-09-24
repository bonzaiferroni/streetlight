package streetlight.web.shells

import koala.SiteImage
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.CityContent
import streetlight.web.layouts.feedSection
import streetlight.web.ui.mainBody
import streetlight.web.ui.entityHeader

fun FlowContent.cityShell(content: CityContent) {
    mainBody("cityShell.kt") {
        entityHeader(
            entity = content.city,
            descriptor = "a city",
            image = content.city.image ?: SiteImage.PearlStreet,
        )
        feedSection(content.feed, cityId = content.city.cityId)
    }

    dataIsland(CityShell.IslandId, content)
}

object CityShell {
    val IslandId = Id("city-shell__island")
}
