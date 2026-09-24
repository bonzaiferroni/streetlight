package streetlight.web.ui

import koala.modifier.Accent
import koala.LottieFile
import koala.dom.*
import koala.model.dedupNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.ui.EventScoutRoute
import koala.html.AppRoute
import koala.model.FetcherContent
import koala.model.toContentOrNull
import koala.modifier.AlignItemsEnd
import streetlight.model.ui.HomeRoute
import streetlight.web.layouts.postRow
import streetlight.web.layouts.route
import streetlight.web.model.EventScoutStage

fun ViewScope.viewEventScout(galaxy: Galaxy?, isAdmin: Boolean) {
    val locationEditor = app.getLocationEditor(LocationEdit(), contentScope)
    val locationScout = app.getLocationScout(galaxy, locationEditor, contentScope)
    val editor = app.getEventEditor(EventEdit(timeZoneId = getTimeZoneId()), contentScope)
    val model = app.getEventScout(galaxy, editor, locationScout, contentScope)
    val doneRoute: AppRoute = galaxy?.route ?: HomeRoute
    val routeFlow = model.stateFlow.dedupNotNull { state -> doneRoute.takeIf { state.isPosted } }
    goOnRoute(routeFlow)

    fun isHeadingStage(stage: EventScoutStage) = when (stage) {
        EventScoutStage.LocationSearch, EventScoutStage.EventSearch, EventScoutStage.Post -> true
        else -> false
    }

    configBody("Event", "Scout", "viewEventScout.kt") {
        configHeading(galaxy?.name ?: "Event", doneRoute)

        stageBlock(model.stage, isHeadingStage = ::isHeadingStage) { stage ->
            when (stage) {
                EventScoutStage.LocationSearch -> formBodyProto {
                    locationFinder(locationScout)
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmit("Next", model::submitLocation, locationEditor.messages, Accent)
                }
                EventScoutStage.EventSearch -> formBodyProto {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor, isAdmin)
                    formSubmit("Next", model::review, editor.message, Accent)
                }
                EventScoutStage.Post -> formBodyProto {
                    val location = locationScout.stateNow.location ?: error("location not found")
                    postRow(editor.editNow, location)
                    column(AlignItemsEnd) {
                        checkBox(model.postAndResetState, "post and start over")
                        formSubmit("Post", model::post, model.postMessage, Accent)
                    }
                }
            }
        }
    }
}

fun RouteScope.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, FetcherContent> { content ->
        starGate { star ->
            viewEventScout(content.toContentOrNull<Galaxy>(), star.isAdmin)
        }
    }
}