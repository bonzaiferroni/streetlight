package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.textBlock
import kotlinx.coroutines.launch
import streetlight.model.data.NewLocation

fun RenderContext.viewMapSandbox(app: AppContext) {
    val eventMap = app.home.eventMap

    button("location query") {
        renderScope.launch {
            // val info = app.client.location.readPlaceInfo(eventMap.stateNow.center)
            // console.log(jsonPrettyConfig.encodeToString(info))
            val locations = app.client.location.queryLocation(eventMap.stateNow.center)
            console.log(locations?.joinToString(", ") { it.name })
        }
    }
    button("create location") {
        renderScope.launch {
            val point = eventMap.stateNow.center
            val info = app.client.location.readPlaceInfo(point)
            console.log(info)
            val id = app.client.location.createLocation(NewLocation(
                name = info.name.takeIf { it.isNotBlank() } ?: info.address.road ?: info.addressType,
                geoPoint = point
            ))
            console.log(id?.value)
        }
    }

    flowBlock(eventMap.focusFlow) { (location, event) ->
        column {
            textBlock("Event: ${event?.title}")
            textBlock("Location: ${location?.name}")
        }
    }
}