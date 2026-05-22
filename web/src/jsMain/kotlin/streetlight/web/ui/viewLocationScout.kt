package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.postCardOf
import streetlight.web.model.LocationScoutStage

fun RenderContext.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), renderScope)
    val model = app.getLocationScout(galaxy, editor, renderScope)
    goOnPosted(model.postFlow)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        stageBlock(model.stageFlow, model::setStage) { stage ->
            when (stage) {
                LocationScoutStage.Search -> formBody {
                    locationScoutForm(model)
                }
                LocationScoutStage.Edit -> column {
                    locationEditFormBody(editor)
                    formSubmit("Done", model::review, messages = model.postMessage)
                }
                LocationScoutStage.Post -> column {
                    val location = model.stateNow.location ?: error("location not found")
                    postCardOf(location)

                    formSubmit("Post", model::postToGalaxy, messages = model.postMessage)
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