package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import koala.html.section
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.model.data.Galaxy
import streetlight.web.LocationScoutRoute
import streetlight.web.io.handleResponse
import streetlight.web.model.LocationFinder
import streetlight.web.model.LocationScout
import streetlight.web.model.Streetlight

fun RenderContext.viewLocationScout(app: Streetlight, galaxy: Galaxy) {
    val finder = LocationFinder(renderScope, app)
    val model = LocationScout(renderScope, app, finder, galaxy)
    val locationFlow = finder.stateFlow.mapDistinct { it.location }
    val textFlow = model.stateFlow.mapDistinct { it.text }

    val sectionMod = modify()

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

            column { // required for boxing FlowContent
                section {
                    filigree {
                        heading3("Post ${location.name}")
                    }

                    card(modify(ZenBg)) {
                        textEditor("text", onValue = model::setText, flow = textFlow)
                        row(modify(JustifyContentSpaceBetween)) {
                            spacer()
                            button("Post", modify(Accent), model::createPost)
                        }
                    }
                }
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewLocationScout.kt")
    }
}

fun ViewContext<Streetlight>.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScout(model, galaxy)
    }
}