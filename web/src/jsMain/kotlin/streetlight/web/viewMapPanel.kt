package streetlight.web

import koala.css.AlignItemsCenter
import koala.css.Animate
import koala.css.MagicBlur
import koala.css.Css
import koala.css.CircleShape
import koala.css.Height3
import koala.css.SlideX
import koala.css.Width100
import koala.css.modify
import koala.dom.*
import koala.html.heading2
import koala.html.image
import koala.html.label
import koala.model.mapDistinct
import kotlinx.html.js.div

fun RenderContext.viewMapPanel(app: AppContext) {
    box(GeoMapIds.panel, modify(Width100)) {
        column {
            viewMapConfig(app)
            viewMapCards(app)
        }
    }
}

fun RenderContext.viewMapCards(app: AppContext) {
    val eventMap = app.streetMap

    flowBlock(eventMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
        box(modify(MapPanel.container)) {

            layers.forEach { layer ->
                val eventType = layer.eventType ?: return@forEach
                mapPanelCard(layer.label) {
                    itemsBlock(eventMap.flowOf(eventType), modifyCardItems, true) { event ->
                        textBlock(event.title)
                    }
                }
            }

            mapPanelCard("Communities") {
                itemsBlock(eventMap.communityFlow, modifyCardItems, true) { community ->
                    box(modify(MapPanel.grid, AlignItemsCenter, Height3)) {
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

private val modifyCardItems = modify(Animate, MagicBlur, SlideX)

fun RenderContext.mapPanelCard(label: String, block: RenderContext.() -> Unit) {
    card(modify(MapPanel.card)) {
        // header
        box(modify(MapPanel.grid, AlignItemsCenter)) {
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