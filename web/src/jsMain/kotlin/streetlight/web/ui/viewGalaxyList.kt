package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.SiteImage
import koala.dom.*
import koala.html.Id
import koala.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.layouts.entityRow
import streetlight.web.shells.cardOf

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