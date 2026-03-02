package streetlight.web.ui

import koala.css.AlignItemsCenter
import koala.css.Magic
import koala.css.Blur
import koala.css.Css
import koala.css.CircleShape
import koala.css.Height3
import koala.css.SlideX
import koala.css.Width100
import koala.css.modify
import koala.dom.*
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.heading2
import koala.html.image
import koala.html.label
import koala.model.mapDistinct
import kotlinx.html.js.div
import streetlight.web.model.AppContext

fun RenderContext.wireMapPanel(app: AppContext) {
    wireBlock(GeoMapSelector.panel) {
        tabs(Id("map-panel-tabs")) {
            tab("News") {
                column {
                    textBlock("yer news")
                }
            }
            tab("Events") {
                column {
                    viewMapCards(app)
                }
            }
            tab("More") {
                column {
                    viewMapConfig(app)
                }
            }
        }
    }
}

fun RenderContext.viewMapCards(app: AppContext) {
    val eventMap = app.streetMap

    flowBlock(eventMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
        container(modify(MapPanel.container)) {

//            layers.forEach { layer ->
//                val eventType = layer.eventType ?: return@forEach
//                mapPanelCard(layer.label) {
//                    itemsBlock(eventMap.flowOf(eventType), modifyCardItems, true) { event ->
//                        textBlock(event.title)
//                    }
//                }
//            }

            mapPanelCard("Communities") {
                itemsBlock(eventMap.communityFlow, modifyCardItems, true) { community ->
                    container(modify(MapPanel.grid, AlignItemsCenter, Height3)) {
                        image(modifiers = modify(CircleShape, Width100))
                        textBlock(community.name)
                        textBlock("4 PM")
                        textBlock("vis")
                    }
                }
            }
        }
    }
}

private val modifyCardItems = modify(Magic, Blur, SlideX)

fun RenderContext.mapPanelCard(label: String, block: RenderContext.() -> Unit) {
    card(modify(MapPanel.card)) {
        // header
        container(modify(MapPanel.grid, AlignItemsCenter)) {
            heading2(label, modify(MapPanel.cardHeading))
            div { }
            label("visibility")
            label("starts")
        }
        block()
    }
}

object MapPanel {
    val container = Css("map-event-panel")
    val card = Css("map-panel-card")
    val cardHeading = Css("map-panel-card-heading")
    val grid = Css("map-panel-card-grid")
}