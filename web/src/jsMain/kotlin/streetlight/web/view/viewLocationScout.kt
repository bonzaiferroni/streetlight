package streetlight.web.view

import koala.dom.*
import koala.model.MenuAction
import koala.utility.dedupNotNull
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import koala.model.WebRoute
import koala.model.FetcherContent
import koala.model.toContentOrNull
import koala.modifier.AlignItemsEnd
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.layout.postRow
import streetlight.web.model.route
import streetlight.web.model.LocationScoutStage
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.feedRow
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getLocationEditor
import streetlight.web.model.getLocationScout

context(star: Star)
fun ViewScope.viewLocationScout(galaxy: Galaxy?) {
    // val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationScout(galaxy, editor, contentScope)
    val doneRoute: WebRoute = galaxy?.route ?: HomeRoute()
    val routeFlow = model.stateFlow.dedupNotNull { state -> doneRoute.takeIf { state.isPosted } }
    goOnRoute(routeFlow)

    configBody("Location", "Scout", "viewLocationScout.kt") {
        configHeading(galaxy?.name ?: "Location", doneRoute)

        stageBlock(model.stageState) { stage ->
            when (stage) {
                LocationScoutStage.Search -> column {
                    context(model) {
                        locationFinder()
                    }
                    if (star.isAdmin) context(model.leadEditor) {
                        leadForm()
                    }
                }
                LocationScoutStage.Edit -> column {
                    context(editor) {
                        locationEditFormBody()
                    }
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
    starRouteBlock<LocationScoutRoute, FetcherContent> { content ->
        viewLocationScout(content.toContentOrNull<Galaxy>())
    }
}

// fun RenderContext.viewStagedEditorForm(editor: LocationEditor)