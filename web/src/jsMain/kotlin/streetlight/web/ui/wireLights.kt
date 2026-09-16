package streetlight.web.ui

//fun <Id> ViewScope.wireLights(
//    root: HTMLElement,
//    attribute: Attribute<Id>,
//    cache: StarCache<Id, *>
//) {
//    val pairs = root.queryAttributeAll(attribute)
//
//    contentScope.launch {
//        launch {
//            // modify flame
//            cache.lightsFlow.collect { lights ->
//                pairs.forEach { (element, itemId) ->
//                    when (lights.any { it == itemId }) {
//                        true -> element.modify(StarLightKey.IsLit)
//                        else -> element.unmodify(StarLightKey.IsLit)
//                    }
//                }
//            }
//        }
//    }
//
//    pairs.forEach { (element, id) ->
//        element.onClick {
//            val isLit = cache.toggleLight(id)
//            element.modifyCounter(if (isLit) 1 else -1)
//        }
//    }
//}

//private fun HTMLElement.modifyCounter(delta: Int) {
//    val counterElement = querySelector(StarLightKey.LightCounter) ?: return
//    val currentCount = counterElement.textContent?.toIntOrNull() ?: 0
//    counterElement.textContent = (currentCount + delta).coerceIn(0, Int.MAX_VALUE).toString()
//}