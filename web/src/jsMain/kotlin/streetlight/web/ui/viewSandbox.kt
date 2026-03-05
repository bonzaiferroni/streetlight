package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.onView
import koala.dom.textBlock
import streetlight.web.model.AppContext

fun RenderContext.viewSandbox(app: AppContext) {
    val element = this@viewSandbox.textBlock("yer element")
    element.onView {
        console.log("In view, matey!")
    }
}

//    row(modify(FlexItemsBasis50)) {
//        textBlock("hello sandbox")
//        viewGeoMap(app.home.geoMap)
//    }
//    button("Go home", onClick = {
//        app.portal.go(HomeRoute())
//    })

//    textField("input")

//    val flow = MutableStateFlow(listOf("One", "Two", "Three"))
//
//    itemsBlock(flow, defaultMagic, animate = true) {
//        textBlock(it)
//    }
//
//    renderScope.launch {
//        delay(2000)
//        flow.value -= "Two"
//        delay(2000)
//        flow.value += "Four"
//        delay(2000)
//        flow.value += "Five"
//        delay(2000)
//        flow.value = flow.value.sorted()
//    }

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
