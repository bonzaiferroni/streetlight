package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.blockLabel
import koala.html.textBlock
import koala.model.mapDistinct
import streetlight.web.EditEventIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.StreetMapLayer

fun RenderContext.viewMapControls(app: Streetlight) {
    val gateAgent = app.gateAgent
    val streetMap = app.streetMap
    val portal = app.portal

    column {
        row(modify(Width100, AlignItemsStart)) {
//            card(modify(Flex1)) {
//                blockLabel = "layers"
//                flowBlock(streetMap.stateFlow.mapDistinct { it.layers }, modify(Width100)) { layers ->
//                    row(modify(JustifySpaceAround, WrapFlex)) {
//                        StreetMapLayer.entries.forEach { layer ->
//                            val isActive = layers.contains(layer)
//                            action({ streetMap.toggleLayer(layer) }) {
//                                row(modify(NoWrap)) {
//                                    this.textBlock(if (isActive) "👁" else "⌣", modify(Dim, Width2, TextAlignCenter))
//                                    this.textBlock(layer.label, if (!isActive) modify(Dim) else null)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            button("Add Event", modify(Accent), onClickEvent = {
                gateAgent.checkIn {
                    portal.go(EditEventIdRoute())
                }
            })
        }
        textBlock("Spirit vision allows you to see the focus point of other spirits and they can see yours.")
        switch("👻 Spirit Vision", onToggle = streetMap::spiritVision)
    }
}