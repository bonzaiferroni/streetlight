package streetlight.web.ui

import koala.LottieFile
import koala.dom.*
import koala.html.topLogo
import koala.model.dedupNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.web.layouts.postRow
import streetlight.web.model.EventScoutStage

fun ViewScope.viewEventScout(galaxy: Galaxy, isAdmin: Boolean) {
    val locationEditor = app.getLocationEditor(LocationEdit(), contentScope)
    val locationScout = app.getLocationScout(galaxy, locationEditor, contentScope)
    val editor = app.getEventEditor(EventEdit(timeZoneId = getTimeZoneId()), contentScope)
    val model = app.getEventScout(galaxy, editor, locationScout, contentScope)
    val routeFlow = model.stateFlow.dedupNotNull { it.postId?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

    fun isHeadingStage(stage: EventScoutStage) = when (stage) {
        EventScoutStage.LocationSearch, EventScoutStage.EventSearch, EventScoutStage.Post -> true
        else -> false
    }

    section(BodyStyle.column) {
        topLogo()

        introSection("Event Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post an event to ${galaxy.name}.")
        }

        stageBlock(model.stage, isHeadingStage = ::isHeadingStage) { stage ->
            when (stage) {
                EventScoutStage.LocationSearch -> formBodyProto {
                    locationScoutForm(locationScout)
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmitLegacy("Next", model::submitLocation, messages = locationEditor.messages)
                }
                EventScoutStage.EventSearch -> formBodyProto {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor, isAdmin)
                    formSubmitLegacy("Next", model::review, messages = editor.message)
                }
                EventScoutStage.Post -> formBodyProto {
                    val location = locationScout.stateNow.location ?: error("location not found")
                    postRow(editor.editNow, location)
                    formSubmitLegacy("Post", model::post, messages = model.postMessage)
                }
            }
        }

        appFooter("")
    }
}

fun RouteScope.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy> { galaxy ->
        starGate { star ->
            viewEventScout(galaxy, star.isAdmin)
        }
    }
}