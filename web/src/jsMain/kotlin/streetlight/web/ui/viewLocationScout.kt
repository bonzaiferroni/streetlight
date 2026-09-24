package streetlight.web.ui

import kampfire.api.Username
import koala.dom.*
import koala.dom.MenuAction
import koala.model.dedupNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import koala.html.AppRoute
import koala.model.FetcherContent
import koala.model.toContentOrNull
import koala.modifier.AlignItemsEnd
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.layouts.postRow
import streetlight.web.layouts.route
import streetlight.web.model.LocationScoutStage

fun ViewScope.viewLocationScout(galaxy: Galaxy?, username: Username) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationScout(galaxy, editor, contentScope)
    val doneRoute: AppRoute = galaxy?.route ?: HomeRoute
    val routeFlow = model.stateFlow.dedupNotNull { state -> doneRoute.takeIf { state.isPosted } }
    goOnRoute(routeFlow)

    configBody("Location", "Scout", "viewLocationScout.kt") {
        configHeading(galaxy?.name ?: "Location", doneRoute)

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
                    model.stateNow.location?.let {
                        feedRow(it, true)
                    } ?: editor.editNow.let {
                        val edit = editor.editNow
                        postRow(edit, username)
                    }

                    column(AlignItemsEnd) {
                        checkBox(model.postAndResetState, "post and start over")
                        formSubmit(
                            label = "Post",
                            onClick = model::post,
                            messenger = editor.messages,
                            back = MenuAction("Edit") { model.stageField.set(LocationScoutStage.Edit) }
                        )
                    }
                }
            }
        }
    }
}

fun RouteScope.viewLocationScoutRoute() {
    starRouteBlock<LocationScoutRoute, FetcherContent> { star, content ->
        viewLocationScout(content.toContentOrNull<Galaxy>(), star.username)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)