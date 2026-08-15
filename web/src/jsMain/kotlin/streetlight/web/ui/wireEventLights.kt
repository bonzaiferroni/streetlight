package streetlight.web.ui

//fun ViewContext<Streetlight>.wireEventLights(root: HTMLElement) {
//    val app = model
//    val cache = app.cache.event
//    val pairs = root.queryAttributeAll(LightKey.EventLightId) { EventId(it) }
//
//    renderScope.launch {
//        cache.lightFlow.collect { lights ->
//            pairs.forEach { (element, eventId) ->
//                val isLit = lights.any { it.eventId == eventId }
//                element.setAttribute(LightKey.LightStatus.to(isLit))
//            }
//        }
//    }
//
//    pairs.forEach { (element, eventId) ->
//        element.onClick {
//            val light = cache.stateNow.lights.firstOrNull { it.eventId == eventId}?.let { star ->
//                star.copy(value = when (star.value) {
//                    null -> LightType.Light
//                    else -> null
//                })
//            } ?: EventLight(eventId, LightType.Light)
//
//            cache.editLight(light)
//        }
//    }
//}