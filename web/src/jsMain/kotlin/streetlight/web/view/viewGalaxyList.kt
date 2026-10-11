package streetlight.web.view

import koala.SiteImage
import koala.dom.*
import koala.page.Id
import streetlight.web.layout.feedRow
import koala.dom.layout.*
import streetlight.web.model.api
import streetlight.web.dom.component.comingSoon
import streetlight.web.dom.layout.dataBlock
import streetlight.web.dom.layout.feedRow
import streetlight.web.dom.layout.mainBody
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

fun ViewScope.viewGalaxyList() {
    mainBody("viewGalaxyList.kt") {
        pageHeader("Galaxies", "Streetlight Communities", SiteImage.HelixNebula)

        lazyTabs(Id("galaxy-list-tabs")) {
            tab("My Galaxies") {
                dataBlock(api.galaxy::readUserGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            feedRow(it)
                        }
                    }
                }
            }
            tab("Top Galaxies") {
                dataBlock(api.galaxy::readTopGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            feedRow(it)
                        }
                    }
                }
            }
            tab("Search") {
                comingSoon()
            }
        }
    }
}