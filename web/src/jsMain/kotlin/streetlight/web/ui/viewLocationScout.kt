package streetlight.web.ui

import koala.dom.*
import koala.model.MenuAction
import koala.utility.dedupNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import koala.model.AppRoute
import koala.model.FetcherContent
import koala.model.toContentOrNull
import koala.modifier.AlignItemsEnd
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.layouts.postRow
import streetlight.web.layouts.route
import streetlight.web.model.LocationScoutStage
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*

fun ViewScope.viewLocationScout(galaxy: Galaxy?, star: Star) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationScout(galaxy, editor, contentScope)
    val doneRoute: AppRoute = galaxy?.route ?: HomeRoute()
    val routeFlow = model.stateFlow.dedupNotNull { state -> doneRoute.takeIf { state.isPosted } }
    goOnRoute(routeFlow)

    configBody("Location", "Scout", "viewLocationScout.kt") {
        configHeading(galaxy?.name ?: "Location", doneRoute)

        stageBlock(model.stageState) { stage ->
            when (stage) {
                LocationScoutStage.Search -> column {
                    locationFinder(model)
                    if (star.isAdmin) leadForm(model.leadEditor)
                }
                LocationScoutStage.Edit -> column {
                    locationEditFormBody(editor)
                    formSubmit(
                        label = "Next",
                        onClick = model::review,
                        messenger = editor.messages,
                        back = MenuAction("Back") { model.stageState.set(LocationScoutStage.Search) }
                    )
                }
                LocationScoutStage.Review -> column {
                    model.stateNow.location?.let {
                        feedRow(it)
                    } ?: postRow(editor.editNow, star.username)

                    column(AlignItemsEnd) {
                        dropMenu(model.postModeState)
                        formSubmit(
                            label = "Post",
                            onClick = model::post,
                            messenger = editor.messages,
                            back = MenuAction("Edit") { model.stageState.set(LocationScoutStage.Edit) }
                        )
                    }
                }
            }
        }
    }
}

fun RouteScope.viewLocationScoutRoute() {
    starRouteBlock<LocationScoutRoute, FetcherContent> { star, content ->
        viewLocationScout(content.toContentOrNull<Galaxy>(), star)
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)