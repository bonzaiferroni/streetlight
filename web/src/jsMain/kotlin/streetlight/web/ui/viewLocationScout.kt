package streetlight.web.ui

import koala.LottieFile
import koala.dom.*
import koala.dom.MenuAction
import koala.model.dedupNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.layouts.postRow
import streetlight.web.model.LocationScoutStage

fun ViewScope.viewLocationScout(galaxy: Galaxy) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationScout(galaxy, editor, contentScope)
    val routeFlow = model.stateFlow.dedupNotNull { it.postId?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

    column {
        introSection("Location Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post a location.")
        }

        stageBlock(model.stageField) { stage ->
            when (stage) {
                LocationScoutStage.Search -> locationFinder(model)
                LocationScoutStage.Edit -> column {
                    locationEditFormBody(editor)
                    formSubmit(
                        label = "Next",
                        onClick = model::review,
                        messenger = editor.messages,
                        back = MenuAction("Back") { model.stageField.set(LocationScoutStage.Search) }
                    )
                }
                LocationScoutStage.Review -> column {
                    val edit = editor.editNow
                    postRow(edit, session.stateNow.star?.username)

                    formSubmit(
                        label = "Post",
                        onClick = model::postToGalaxy,
                        messenger = editor.messages,
                        back = MenuAction("Edit") { model.stageField.set(LocationScoutStage.Edit) }
                    )
                }
            }
        }

        appFooter("")
    }
}

fun RouteScope.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy> { galaxy ->
        viewLocationScout(galaxy)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)