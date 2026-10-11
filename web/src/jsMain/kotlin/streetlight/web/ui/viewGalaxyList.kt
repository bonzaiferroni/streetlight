package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import koala.page.Id
import streetlight.web.layouts.feedRow
import koala.dom.layout.*

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