package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.propertyValue
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.LocationEdit
import streetlight.web.model.*
import streetlight.web.shells.cardOf

fun RenderContext.viewScoutMap(app: AppContext) {
    val locationsFlow = app.streetMap.locationsFlow
    val model = ScoutMap(renderScope, app.client.api, app.geoMap)

    renderScope.launch {
        locationsFlow.collect {
            console.log("locations ${it.size}")
        }
    }

    column {
        viewGeoMap(app.geoMap, app.appScope)

        card {
            row {
                textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.link })
                button("🤖 read link", onClick = model::readLink)
                button("📝 editor")
            }
        }

        flowBlock(model.editFlow, modify(Blur, SlideX), magic = true) { edit ->
            card {
                if (edit != null) {
                    column {
                        row {
                            textBlock("Does this information look correct?", modify(Flex1))
                            button("Needs edits")
                            button("Looks good", modify(Accent), onClick = {
                                renderScope.launch {
                                    val location = model.postLocation()
                                    if (location != null) {
                                        console.log(location)
                                    }
                                }
                            })
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
                } else {
                    messageBox(model.messageFlow)
                }
            }
        }

        itemsBlock(locationsFlow, modify(Blur, SlideX), magic = true) { (location, events) ->
            box {
                cardOf(location)
            }
        }
    }
}
