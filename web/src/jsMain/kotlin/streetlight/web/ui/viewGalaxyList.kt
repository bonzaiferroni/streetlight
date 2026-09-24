package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import koala.html.Id
import streetlight.web.layouts.feedRow
import streetlight.web.pages.appHeader

fun ViewScope.viewGalaxyList() {
    shellBody("viewGalaxyList.kt") {
        pageHeader("Galaxies", "Streetlight Communities", SiteImage.HelixNebula)

        lazyTabs(Id("galaxy-list-tabs")) {
            tab("My Galaxies") {
                dataBlock(api.galaxy::readUserGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            feedRow(it, true)
                        }
                    }
                }
            }
            tab("Top Galaxies") {
                dataBlock(api.galaxy::readTopGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            feedRow(it, true)
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