package streetlight.web.ui

import koala.css.Accent
import koala.css.AlignItemsEnd
import koala.css.AlignItemsStart
import koala.css.AlignSelfStart
import koala.css.CenterItems
import koala.css.Dim
import koala.css.Flex1
import koala.css.Flex2
import koala.css.JustifyEnd
import koala.css.JustifySelfEnd
import koala.css.JustifySpaceBetween
import koala.css.Secondary
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.ViewContext
import koala.dom.box
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.defaultMagic
import koala.dom.dialogBox
import koala.dom.flowBlock
import koala.dom.image
import koala.dom.itemsBlock
import koala.dom.messageBox
import koala.dom.open
import koala.dom.row
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.dom.viewGeoMap
import koala.dom.viewOf
import koala.html.heading3
import koala.html.heading4
import koala.html.propertyValue
import koala.html.spacer
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.web.EventScoutRoute
import streetlight.web.model.EventScout
import streetlight.web.model.Streetlight

fun RenderContext.viewEventScout(app: Streetlight, galaxy: Galaxy) {
    val model = EventScout(app, renderScope)
    val panelFlow = model.stateFlow.mapDistinct {
        EventScoutPanelState(
            locationEdit = it.locationEdit,
            location = it.location,
        )
    }

    column {
        viewGeoMap(app.geoMap, app.appScope)

        flowBlock(panelFlow, defaultMagic, magic = true) {
            val locationEdit = it.locationEdit; val location = it.location

            viewOf(model) {
                if (location != null) {
                    createEventPanel(location)
                } else if (locationEdit != null) {
                    reviewLocationPanel(locationEdit)
                } else {
                    findLocationPanel()
                }
            }
        }

        appFooter()
    }
}

private data class EventScoutPanelState(
    val locationEdit: LocationEdit? = null,
    val location: Location? = null,
)

fun ViewContext<Streetlight>.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->
        viewEventScout(model, galaxy)
    }
}

fun ViewContext<EventScout>.createEventPanel(location: Location?) {
    val editFlow = model.stateFlow.mapDistinct { it.eventEdit }
    val dialogOpen = storeOf(false)

    val dialog = dialogBox("Edit Event") {
        viewEventEditor(model.stateNow.eventEdit, model.app, editFlow.filterNotNull(), model::setEventEdit)
    }

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readEventWebsite)
            }
        }

        card {
            box(modify(Width100)) {
                flowBlock(editFlow) { edit ->
                    if (edit != null) {
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
                                heading3(edit.title ?: "[No title found]")
                                textBlock(edit.description ?: "[No description]")
                            }
                        }
                    } else {
                        textBlock("Read website or enter details.")
                    }
                }
                button("Edit", modify(JustifySelfEnd, AlignSelfStart), onClick = {
                    // dialogOpen.set { true }
                    dialog.open()
                })
            }
        }
    }
}

fun ViewContext<EventScout>.reviewLocationPanel(locationEdit: LocationEdit) {
    val editFlow = model.stateFlow.mapDistinct { it.locationEdit }.filterNotNull()

    column {
        card {
            messageBox(model.messageFlow)
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readLocationWebsite)
            }
        }

        tabs {
            tab("Details") {
                flowBlock(editFlow) { edit ->
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
                            propertyValue("website", edit.website ?: "[No website]")
                            propertyValue("calendar", edit.eventsUrl ?: "[No calendar]")
                            propertyValue("about", edit.aboutUrl ?: "[No about]")
                            propertyValue("menu", edit.menuUrl ?: "[No menu]")
                        }
                    }
                }
            }
            tab("Edit") {
                viewLocationEditor(locationEdit, model.app, editFlow) {
                    model.setEdit(it)
                }
            }
        }

        row(modify(JustifySpaceBetween)) {
            button("start over", modify(Secondary), onClick = model::reset)
            button("create location", modify(Accent), onClick = model::postLocation)
        }
    }
}

fun ViewContext<EventScout>.findLocationPanel() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val locationsFlow = model.stateFlow.mapDistinct { it.locations }

    card {
        messageBox(model.messageFlow)
        row(modify(JustifyEnd)) {
            textBlock("Move the map target to the location.", modify(Dim))
            button("Here", onClick = model::here)
        }

        spacer("or")

        heading4("Search")
        textBlock("We can search for the location's name, address, city, etc.", modify(Dim))

        row(modify(AlignItemsStart)) {
            column(modify(Flex1, AlignItemsEnd)) {
                textField("search", modify(Width100), model::setQuery, queryFlow, placeholder = "Search by name or address")
            }
            button("Search", onClick = model::searchQuery)
        }

        itemsBlock(locationsFlow) { location ->
            cardOf(location) {
                model.setLocation(location)
            }
        }
    }
}