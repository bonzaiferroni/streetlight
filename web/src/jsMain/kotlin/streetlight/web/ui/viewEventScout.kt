package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.*
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.EventScoutRoute
import streetlight.web.model.EventScoutStage

fun RenderContext.viewEventScout(galaxy: Galaxy) {
    val locationEditor = app.getLocationEditor(LocationEdit(), renderScope)
    val location = app.getLocationScout(galaxy, locationEditor, renderScope)
    val editor = app.getEventEditor(EventEdit(), renderScope)
    val model = app.getEventScout(galaxy, editor, location, renderScope)
    goOnPosted(model.postFlow)

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
                    formSubmit("Done", location::review)
                }
                EventScoutStage.EventSearch -> TODO()
                EventScoutStage.EventEdit -> TODO()
                EventScoutStage.Post -> TODO()
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