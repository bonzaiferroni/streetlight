package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import koala.html.Id
import streetlight.web.layouts.entityRow

fun ViewScope.viewGalaxyList() {

    column(mod = BodyStyle.column) {
        featureHeader("Galaxies", "Streetlight Communities", SiteImage.HelixNebula)

        tabs(Id("galaxy-list-tabs")) {
            tab("My Galaxies") {
                dataBlock(api::readUserGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            entityRow(it, true)
                        }
                    }
                }
            }
            tab("Top Galaxies") {
                dataBlock(api::readTopGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            entityRow(it, true)
                        }
                    }
                }
            }
            tab("Search") {
                textBlock("Coming soon")
            }
        }
    }
}