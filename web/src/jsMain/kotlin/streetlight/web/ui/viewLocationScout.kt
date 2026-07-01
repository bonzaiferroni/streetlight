package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.LottieFile
import koala.dom.*
import koala.model.mapDistinctNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.GalaxyRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.layouts.feedPostOf
import streetlight.web.model.LocationScoutStage

fun AppScope.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), parentScope)
    val model = app.getLocationScout(galaxy, editor, parentScope)
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
                    feedPostOf(edit, session.stateNow.star?.username)

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

fun AppScope.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleOutcome(toaster::toast)
    }) { galaxy ->
        viewLocationScout(galaxy)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)