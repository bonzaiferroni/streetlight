package streetlight.web.view

import koala.modifier.Accent
import koala.LottieFile
import koala.dom.*
import koala.utility.dedupNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import streetlight.model.ui.EventScoutRoute
import koala.model.AppRoute
import koala.model.FetcherContent
import koala.model.toContentOrNull
import koala.modifier.AlignItemsEnd
import streetlight.model.ui.HomeRoute
import streetlight.web.layout.postRow
import streetlight.web.model.route
import streetlight.web.model.EventScoutStage
import koala.dom.component.*
import koala.dom.layout.*
import koala.utility.*
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.feedRow
import streetlight.web.dom.layout.formBodyProto
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getEventEditor
import streetlight.web.model.getEventScout
import streetlight.web.model.getLocationEditor
import streetlight.web.model.getLocationScout

fun ViewScope.viewEventScout(galaxy: Galaxy?, star: Star) {
    val locationEditor = app.getLocationEditor(LocationEdit(), contentScope)
    val locationScout = app.getLocationScout(galaxy, locationEditor, contentScope)
    val editor = app.getEventEditor(EventEdit(timeZoneId = getTimeZoneId()), contentScope)
    val model = app.getEventScout(galaxy, editor, locationScout, contentScope)
    val doneRoute: AppRoute = galaxy?.route ?: HomeRoute()
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
                    column {
                        locationFinder(locationScout)
                        if (star.isAdmin) leadForm(model.leadEditor)
                    }
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmit("Next", model::submitLocation, locationEditor.messages, Accent)
                }
                EventScoutStage.EventSearch -> formBodyProto {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor, star.isAdmin)
                    formSubmit("Next", model::review, editor.message, Accent)
                }
                EventScoutStage.Post -> formBodyProto {
                    model.stateNow.event?.let {
                        feedRow(it)
                    } ?: run {
                        val location = locationScout.stateNow.location ?: error("location not found")
                        postRow(editor.editNow, location, star.username)
                    }
                    column(AlignItemsEnd) {
                        dropMenu(model.postModeState)
                        formSubmit("Post", model::post, model.postMessage, Accent)
                    }
                }
            }
        }
    }
}

fun RouteScope.viewEventScoutRoute() {
    starRouteBlock<EventScoutRoute, FetcherContent> { star, content ->
        viewEventScout(content.toContentOrNull<Galaxy>(), star)
    }
}