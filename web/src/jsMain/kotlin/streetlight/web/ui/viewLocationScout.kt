package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import koala.model.mapDistinctNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.layouts.postRow
import streetlight.web.model.LocationScoutStage

fun ViewScope.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), scope)
    val model = app.getLocationScout(galaxy, editor, scope)
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.postId?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        stageBlock(model.stageFlow, model::setStage) { stage ->
            when (stage) {
                LocationScoutStage.Search -> formBodyProto {
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
                    postRow(edit, session.stateNow.star?.username)

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

fun ViewScope.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)