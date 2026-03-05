package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.propertyValue
import koala.model.mapDistinct
import kotlinx.coroutines.launch
import streetlight.web.ReadEventRoute
import streetlight.web.model.*
import streetlight.web.shells.cardOf

fun RenderContext.viewLocationScout(app: AppContext) {
    val locationsFlow = app.streetMap.locationsFlow
    val model = LocationScout(renderScope, app.client.api, app.geoMap)

    column {
        viewGeoMap(app.geoMap, app.appScope)

        flowBlock(model.editFlow, defaultMagic, magic = true) { edit ->
            if (edit != null) {
                flowBlock(model.locationFlow, defaultMagic, magic = true) { location ->
                    if (location != null) {
                        card {
                            row {
                                messageBox(model.messageFlow, modify(Flex1))
                                button("Start over", onClick = model::reset)
                                button("Post events", modify(Accent), onClick = {
                                    app.portal.go(ReadEventRoute(location))
                                })
                            }
                        }
                    } else {
                        card {
                            row {
                                messageBox(model.messageFlow, modify(Flex1))
                                button("Start over", onClick = model::reset)
                                button("Needs edits")
                                button("Looks good", modify(Accent), onClick = model::postLocation)
                            }
                            row(modify(AlignItemsStart)) {
                                val imageUrl = edit.imageUrl
                                if (imageUrl != null) {
                                    image(imageUrl, modify(Flex1, Width100))
                                } else {
                                    box(modify(Flex1, CenterItems)) {
                                        textBlock("no image")
                                    }
                                }
                                column(modify(Flex2)) {
                                    heading3(edit.name ?: "[No name found]")
                                    textBlock(edit.description ?: "[No description]")
                                    propertyValue("address", edit.address ?: "[No address]")
                                    propertyValue("link", edit.link ?: "[No link]")
                                    propertyValue("calendar", edit.eventsLink ?: "[No calendar]")
                                }
                            }
                        }
                    }
                }
            } else {
                column {
                    card {
                        messageBox(model.messageFlow)
                        row {
                            textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.link })
                            button("🤖 read link", onClick = model::readLink)
                            button("📝 editor")
                        }
                    }

                    itemsBlock(locationsFlow, defaultMagic, magic = true) { (location, events) ->
                        box {
                            cardOf(location)
                        }
                    }
                }
            }
        }
    }

//    flowBlock(model.isMapFlow, defaultMagic, magic = true) { isMap ->
//        if (isMap) {
//
//        } else {
//
//        }
//    }
}
