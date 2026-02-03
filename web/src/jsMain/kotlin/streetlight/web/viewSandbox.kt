package streetlight.web

import koala.dom.RenderContext
import koala.dom.itemsBlock
import koala.dom.textBlock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

fun RenderContext.viewSandbox(app: AppContext) {

    val flow = MutableStateFlow(listOf("One", "Two", "Three"))

    itemsBlock(flow) {
        textBlock(it)
    }

    renderScope.launch {
        delay(2000)
        flow.value -= "Two"
        delay(4000)
        flow.value += "Four"
    }

//    val eventMap = app.home.eventMap
//
//    button("location query") {
//        renderScope.launch {
//            // val info = app.client.location.readPlaceInfo(eventMap.stateNow.center)
//            // console.log(jsonPrettyConfig.encodeToString(info))
//            val locations = app.client.location.queryLocation(eventMap.stateNow.center)
//            console.log(locations?.joinToString(", ") { it.name })
//        }
//    }
//    button("create location") {
//        renderScope.launch {
//            val point = eventMap.stateNow.center
//            val info = app.client.location.readPlaceInfo(point)
//            console.log(info)
//            val id = app.client.location.createLocation(NewLocation(
//                name = info.name.takeIf { it.isNotBlank() } ?: info.address.road ?: info.addressType,
//                geoPoint = point
//            ))
//            console.log(id?.value)
//        }
//    }
//
//    flowBlock(eventMap.focusFlow) { (location, event) ->
//        column {
//            textBlock("Event: ${event?.title}")
//            textBlock("Location: ${location?.name}")
//        }
//    }
}