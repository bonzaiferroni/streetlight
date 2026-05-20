package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.css.*
import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.postCard
import streetlight.web.layouts.postCardOf
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScout
import streetlight.web.model.LocationScoutStage

fun RenderContext.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getCoroutineScoped<LocationEditor>(LocationEdit(), renderScope)
    val model = app.getCoroutineScoped<LocationScout>(galaxy, editor, renderScope)
    goOnPosted(model.postFlow)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        flowBlock(model.stageFlow, modify(Magic, Blur)) { stage ->
            when (stage) {
                LocationScoutStage.Search -> formBody {
                    locationScoutForm(model)
                }
                LocationScoutStage.Editor -> column {
                    formBody {
                        locationWebsiteForm(editor)
                        locationDetailsForm(editor)
                        locationImageForm(editor)
                        locationLinksForm(editor)
                    }
                    row(modify(JustifyContentEnd)) {
                        button("Post", onClick = model::postToGalaxy)
                    }
                }
                LocationScoutStage.Location -> column {
                    val location = model.stateNow.location ?: error("location not found")
                    postCardOf(location)

                    row(modify(JustifyContentEnd)) {
                        button("Post", onClick = model::postToGalaxy)
                    }
                }
            }
        }

        appFooter("")
    }
}

fun RenderContext.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)