package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.paragraph

fun RenderContext.viewMapConfig(app: AppContext) {
    val gateAgent = app.gateAgent
    val eventMap = app.home.eventMap
    val eventCreator = app.home.eventCreator

    row(modify(Width100)) {
        card(modify(Flex1)) {
            flowBlock(eventMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
                row(modify(JustifySpaceAround)) {
                    MapLayer.entries.forEach { layer ->
                        val isActive = layers.contains(layer)
                        action({ eventMap.toggleLayer(layer) }) {
                            row(modify(NoWrap)) {
                                paragraph(if (isActive) "👁" else "⌣", modify(Dim, Width2, TextAlignCenter))
                                paragraph(layer.label, if (!isActive) modify(Dim) else null)
                            }
                        }
                    }
                }
            }
        }
        button("Add Event") {
            gateAgent.checkIn {
                eventCreator.toggle()
            }
        }
    }
}