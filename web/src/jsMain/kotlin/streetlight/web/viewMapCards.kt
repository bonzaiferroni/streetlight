package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.html.h2

fun RenderContext.viewMapCards(app: AppContext) {
    val eventMap = app.home.streetMap

    flowBlock(eventMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
        box(modify(MapPanel.container)) {
            mapPanelCard("Communities") {
                itemsBlock(eventMap.communityFlow, modifyCardItems, true) { community ->
                    textBlock(community.name)
                }
            }

            layers.forEach { layer ->
                val eventType = layer.eventType ?: return@forEach
                mapPanelCard(layer.label) {
                    itemsBlock(eventMap.flowOf(eventType), modifyCardItems, true) { event ->
                        textBlock(event.title)
                    }
                }
            }
        }
    }
}

private val modifyCardItems = modify(Animate, Blur, SlideX)

fun RenderContext.mapPanelCard(label: String, block: RenderContext.() -> Unit) {
    card(modify(MapPanel.card)) {
        h2 {
            +label
        }
        block()
    }
}