package streetlight.web.ui

import koala.SiteImage
import koala.dom.*
import koala.html.Id
import streetlight.web.layouts.entityRow

fun AppScope.viewGalaxyList() {

    column(mod = BodyStyle.Mod) {
        featureHeader("Galaxies", "Streetlight Communities", SiteImage.HelixNebula)

        tabs(Id("galaxy-list-tabs")) {
            tab("My Galaxies") {
                request(api::readUserGalaxies) { galaxies ->
                    column {
                        galaxies.forEach {
                            entityRow(it, true)
                        }
                    }
                }
            }
            tab("Top Galaxies") {
                request(api::readTopGalaxies) { galaxies ->
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