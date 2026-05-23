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

fun RenderContext.viewEventScout(galaxy: Galaxy) {
    val locationEditor = app.getLocationEditor(LocationEdit(), renderScope)
    val location = app.getLocationScout(galaxy, locationEditor, renderScope)
    val editor = app.getEventEditor(EventEdit(), renderScope)
    val model = app.getEventScout(galaxy, editor, location, renderScope)
    val routeFlow = model.stateFlow.mapDistinctNotNull { it.postId?.let { GalaxyRoute(galaxy.slug) } }
    goOnRoute(routeFlow)

    section {
        introSection("Event Scout", lottie = LottieFile.StrollingMan) {
            textBlock("Let's post an event to ${galaxy.name}.")
        }

        stageBlock(model.stageFlow, model::setStage) { stage ->
            when (stage) {
                EventScoutStage.LocationSearch -> formBody {
                    locationScoutForm(location)
                }
                EventScoutStage.LocationEdit -> column {
                    locationEditFormBody(locationEditor)
                    formSubmit("Next", location::review, messages = locationEditor.message)
                }
                EventScoutStage.EventSearch -> formBody {
                    eventSearchForm(model)
                }
                EventScoutStage.EventEdit -> column {
                    eventEditFormBody(editor)
                    formSubmit("Next", model::review, messages = editor.message)
                }
                EventScoutStage.Post -> formBody {
                    val event = model.stateNow.event ?: error("location not found")
                    postCardOf(event)
                    formSubmit("Post", model::post, messages = model.postMessage)
                }
            }
        }
    }
}

fun RenderContext.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        viewEventScout(galaxy)
    }
}