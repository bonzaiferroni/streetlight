package streetlight.web.shells

import koala.SiteImage
import kotlinx.html.FlowContent
import streetlight.model.data.CityContent
import streetlight.model.ui.CityRoute
import streetlight.web.layouts.feedSection
import streetlight.web.ui.mainBody
import streetlight.web.ui.entityHeader
import koala.component.*
import koala.page.*

fun FlowContent.cityShell(content: CityContent) {
    mainBody("cityShell.kt") {
        entityHeader(
            entity = content.city,
            descriptor = "a city",
            image = content.city.image ?: SiteImage.PearlStreet,
        )
        feedSection(content.feed) { CityRoute(content.city.slug, it) }
    }

    dataIsland(CityShell.IslandId, content)
}

object CityShell {
    val IslandId = Id("city-shell__island")
}
