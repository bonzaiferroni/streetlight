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
import streetlight.web.model.LocationFinderProto
import streetlight.web.model.LocationScoutProto
import streetlight.web.model.Streetlight

fun RenderContext.viewLocationScoutProto(app: Streetlight, galaxy: Galaxy) {
    val finder = LocationFinderProto(renderScope, app)
    val model = LocationScoutProto(renderScope, app, finder, galaxy)
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
                viewLocationFinderProto()
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

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewLocationScoutProto.kt")
    }
}

fun ViewContext<Streetlight>.viewLocationScoutRouteProto() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScoutProto(model, galaxy)
    }
}