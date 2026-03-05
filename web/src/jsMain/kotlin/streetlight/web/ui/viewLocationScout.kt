package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.heading4
import koala.html.propertyValue
import koala.model.mapDistinct
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.web.ReadEventRoute
import streetlight.web.model.*
import streetlight.web.shells.cardOf

fun RenderContext.viewLocationScout(app: AppContext) {
    val model = LocationScout(renderScope, app)
    tabs {
        tab("Scout") {
            column {
                viewGeoMap(app.geoMap, app.appScope)

                flowBlock(model.dataFlow, defaultMagic, magic = true) { data ->
                    val point = data.point; val edit = data.edit; val location = data.location; val places = data.places
                    viewOf(model) {
                        if (location != null) {
                            finishedStage(location)
                        } else if (edit != null) {
                            reviewStage(edit)
                        } else if (point != null) {
                            creationStage()
                        } else if (places != null) {
                            choosePlaceStage(places)
                        } else {
                            findPointStage()
                        }
                    }
                }
            }
        }
        tab("More Info") {
            textBlock("yer info")
        }
    }
}

fun ViewContext<LocationScout>.findPointStage() {
    val locationsFlow = model.app.streetMap.locationsFlow
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val limitCityFlow = model.stateFlow.mapDistinct { it.limitCity }
    val limitMapFlow = model.stateFlow.mapDistinct { it.limitMap }
    val limitStateFlow = model.stateFlow.mapDistinct { it.limitState }

    column {
        card {
            row {
                messageBox(model.messageFlow, modify(Flex1))
                button("Here", modify(Accent), onClick = model::here)
            }
        }

        card {
            heading4("OpenStreetMap")
            textBlock("We can also search OpenStreetMap by the location's name, address, city, etc.", modify(Dim))

            row(modify(AlignItemsStart)) {
                column(modify(Flex1, AlignItemsEnd)) {
                    textField("search", modify(Width100), model::setQuery, queryFlow, placeholder = "Search by name or address")
                    row(modify(JustifyEnd)) {
                        textBlock("Limit search area to:", modify(Dim))
                        switch("map", onToggle = model::setLimitMap, bindFlow = limitMapFlow)
                        model.stateNow.city?.let {
                            switch(it, onToggle = model::setLimitCity, bindFlow = limitCityFlow)
                        }
                        model.stateNow.state?.let {
                            switch(it, onToggle = model::setLimitState, bindFlow = limitStateFlow)
                        }
                    }
                }
                button("Search", onClick = model::searchOSM)
            }
        }

        card {
            row {
                heading3("Nearby locations", modify(Flex1))
            }
            itemsBlock(locationsFlow, defaultMagic, magic = true) { (location, events) ->
                box {
                    cardOf(location)
                }
            }
        }
    }
}

fun ViewContext<LocationScout>.choosePlaceStage(places: List<Place>) {
    card {
        row {
            messageBox(model.messageFlow, modify(Flex1))
            button("start over", onClick = model::reset)
        }
        places.forEach { place ->
            val name = place.name ?: return@forEach
            action(onClick = { model.choosePlace(place) }, modify(Width100)) {
                card {
                    textBlock(name)
                    place.address?.let {
                        textBlock(it)
                    }
                }
            }
        }
    }
}

fun ViewContext<LocationScout>.creationStage() {
    column {
        card {
            row {
                messageBox(model.messageFlow, modify(Flex1))
                button("start over", onClick = model::reset)
            }
            row {
                textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.website })
                button("🤖 read link", modify(Accent), onClick = model::readLink)
            }
            row {
                textBlock("Or you can enter the details yourself.", modify(Flex1, Dim))
                button("📝 editor", modify(Accent))
            }
        }
    }
}

fun ViewContext<LocationScout>.finishedStage(location: Location) {
    val portal = model.app.portal

    card {
        row {
            messageBox(model.messageFlow, modify(Flex1))
            button("Start over", onClick = model::reset)
            button("Post events", modify(Accent), onClick = {
                portal.go(ReadEventRoute(location))
            })
        }
    }
}

fun ViewContext<LocationScout>.reviewStage(edit: LocationEdit) {
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