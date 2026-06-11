package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import koala.model.mapDistinctNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.GalaxyRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.postCardOf
import streetlight.web.model.LocationScoutStage

fun DOMRender.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), renderScope)
    val model = app.getLocationScout(galaxy, editor, renderScope)
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.slug?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

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
                    formSubmit(
                        label = "Next",
                        onSubmit = model::review,
                        messages = editor.message,
                        back = LabeledAction("Back", { model.setStage(LocationScoutStage.Search) })
                    )
                }
                LocationScoutStage.Post -> column {
                    val edit = editor.editNow
                    console.log("ey: ${gate.stateNow.star?.username}")
                    postCardOf(edit, gate.stateNow.star?.username)

                    formSubmit(
                        label = "Post",
                        onSubmit = model::postToGalaxy,
                        messages = editor.message,
                        back = LabeledAction("Edit", { model.setStage(LocationScoutStage.Edit) })
                    )
                }
            }
        }

        appFooter("")
    }
}

fun DOMRender.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)