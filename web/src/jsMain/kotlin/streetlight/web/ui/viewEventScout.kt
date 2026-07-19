package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import koala.model.tapNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.ui.EventScoutRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.web.layouts.postRow
import streetlight.web.model.EventScoutStage

fun ViewScope.viewEventScout(galaxy: Galaxy) {
    val locationEditor = app.getLocationEditor(LocationEdit(), scope)
    val locationScout = app.getLocationScout(galaxy, locationEditor, scope)
    val editor = app.getEventEditor(EventEdit(timeZoneId = getTimeZoneId()), scope)
    val model = app.getEventScout(galaxy, editor, locationScout, scope)
    val routeFlow = model.stateFlow.tapNotNull { it.postId?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

    fun isHeadingStage(stage: EventScoutStage) = when (stage) {
        EventScoutStage.LocationSearch, EventScoutStage.EventSearch, EventScoutStage.Post -> true
        else -> false
    }

    section {
        introSection("Event Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post an event to ${galaxy.name}.")
        }

        stageBlock(model.stageFlow, model::setStage, isHeadingStage = ::isHeadingStage) { stage ->
            when (stage) {
                EventScoutStage.LocationSearch -> formBodyProto {
                    locationScoutForm(locationScout)
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmit("Next", model::submitLocation, messages = locationEditor.message)
                }
                EventScoutStage.EventSearch -> formBodyProto {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor)
                    formSubmit("Next", model::review, messages = editor.message)
                }
                EventScoutStage.Post -> formBodyProto {
                    val location = locationScout.stateNow.location ?: error("location not found")
                    postRow(editor.editNow, location)
                    formSubmit("Post", model::post, messages = model.postMessage)
                }
            }
        }

        appFooter("")
    }
}

fun ViewScope.viewEventScoutRoute() {
    starGate { star ->
        routeBlock<EventScoutRoute, Galaxy>({
            api.readGalaxy(it.slug).handleResponse(toaster)
        }) { galaxy ->
            viewEventScout(galaxy)
        }
    }
}