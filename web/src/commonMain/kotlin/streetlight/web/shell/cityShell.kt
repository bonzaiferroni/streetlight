package streetlight.web.shell

import koala.SiteImage
import kotlinx.html.FlowContent
import streetlight.model.data.CityContent
import streetlight.model.ui.CityRoute
import streetlight.web.layout.feedSection
import streetlight.web.layout.mainBody
import streetlight.web.layout.entityHeader
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
