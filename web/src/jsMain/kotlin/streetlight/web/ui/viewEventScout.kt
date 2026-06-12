package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import koala.model.mapDistinctNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.EventScoutRoute
import streetlight.web.GalaxyRoute
import streetlight.web.layouts.postCardOf
import streetlight.web.model.EventScoutStage

fun RenderScope.viewEventScout(galaxy: Galaxy) {
    val locationEditor = app.getLocationEditor(LocationEdit(), parentScope)
    val locationScout = app.getLocationScout(galaxy, locationEditor, parentScope)
    val editor = app.getEventEditor(EventEdit(timeZoneId = getTimeZoneId()), parentScope)
    val model = app.getEventScout(galaxy, editor, locationScout, parentScope)
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.slug?.let { GalaxyRoute(galaxy.slug) } }
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
                EventScoutStage.LocationSearch -> formBody {
                    locationScoutForm(locationScout)
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmit("Next", model::submitLocation, messages = locationEditor.message)
                }
                EventScoutStage.EventSearch -> formBody {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor)
                    formSubmit("Next", model::review, messages = editor.message)
                }
                EventScoutStage.Post -> formBody {
                    val location = locationScout.stateNow.location ?: error("location not found")
                    postCardOf(editor.editNow, location)
                    formSubmit("Post", model::post, messages = model.postMessage)
                }
            }
        }

        appFooter("")
    }
}

fun RenderScope.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewEventScout(galaxy)
    }
}