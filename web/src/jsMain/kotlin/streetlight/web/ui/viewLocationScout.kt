package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.ButtonMenu
import koala.html.buttonMenu
import koala.html.card
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import koala.html.section
import koala.html.spacer
import koala.html.textProperty
import koala.model.mapDistinct
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.web.LocationScoutRoute
import streetlight.web.model.LocationFinder
import streetlight.web.model.LocationScout
import streetlight.web.model.Streetlight

fun RenderContext.viewLocationScout(app: Streetlight, galaxy: Galaxy, galaxies: List<Galaxy>) {
    val sectionMod = modify()
    val finder = LocationFinder(renderScope, app)
    val scout = LocationScout(renderScope, app, finder, galaxy.galaxyId)
    val locationFlow = finder.stateFlow.mapDistinct { it.location }

    column(modify(Gap8)) {
        section(sectionMod) {
            filigree {
                heading1("Location Scout", modify(Shrinkable))
            }
            heading3("Posting to ${galaxy.name}", modify(OpacityMost, TextAlignCenter))
        }

        section(sectionMod) {
            filigree {
                heading3("Find a location")
            }
            viewContextOf(finder) {
                viewLocationFinder()
            }
        }

        flowBlock(locationFlow, defaultMagic) { location ->
            val location = location ?: return@flowBlock
            viewContextOf(scout) {
                postLocationEditor(location, galaxy, galaxies)
            }

        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewLocationScout.kt")
    }
}

fun ViewContext<LocationScout>.postLocationEditor(location: Location, galaxy: Galaxy, galaxies: List<Galaxy>) {
    val titleFlow = model.stateFlow.mapDistinct { it.title }
    val textFlow = model.stateFlow.mapDistinct { it.text }
    val galaxiesFlow = model.stateFlow.mapDistinct { it.galaxyIds }
    val allGalaxies = when (galaxies.any { it.galaxyId == galaxy.galaxyId }) {
        true -> galaxies
        else -> galaxies + galaxy
    }

    column {
        section {
            filigree {
                heading3("Post ${location.name}")
            }

            flowBlock(galaxiesFlow) { galaxyIds ->
                val toGalaxies = allGalaxies.filter { galaxyIds.contains(it.galaxyId) }
                val availableGalaxies = allGalaxies.filter { !galaxyIds.contains(it.galaxyId) }
                val galaxyNames = toGalaxies.joinToString(", ") { it.name }

                row(modify(JustifyContentSpaceBetween)) {
                    textProperty("Post to", galaxyNames)
                    if (availableGalaxies.isNotEmpty()) {
                        buttonMenu("galaxies") {
                            card(ButtonMenu.CardMod) {
                                availableGalaxies.forEach { galaxy ->
                                    button(galaxy.name, onClick = { model.addGalaxyId(galaxy.galaxyId) })
                                }
                            }
                        }
                    }
                }
            }

            textField("title", onValue = model::setTitle, flow = titleFlow)
            textField("text", onValue = model::setText, flow = textFlow)
            row(modify(JustifyContentSpaceBetween)) {
                spacer()
                button("Post", onClick = model::createPost)
            }
        }
    }
}

fun ViewContext<Streetlight>.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->
        viewLocationScout(model, galaxy, model.cache.galaxy.stateNow.galaxies)
    }
}