package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.blockLabel
import koala.html.paragraph
import koala.model.mapDistinct

fun RenderContext.viewMapConfig(app: AppContext) {
    val gateAgent = app.gateAgent
    val eventMap = app.home.streetMap
    val eventCreator = app.home.eventCreator
    val portal = app.portal

    row(modify(Width100, AlignItemsStart)) {
        card(modify(Flex1)) {
            blockLabel = "layers"
            flowBlock(eventMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
                row(modify(JustifySpaceAround, WrapFlex)) {
                    StreetMapLayer.entries.forEach { layer ->
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
        button("Add Event", modify(Accent), onClick = {
            gateAgent.checkIn {
                portal.go(CreateEventRoute)
            }
        })
    }
}