package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.heading4
import koala.html.propertyValue
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.web.ReadEventRoute
import streetlight.web.model.*

fun RenderContext.viewLocationScout(app: AppContext) {
    val model = LocationScout(renderScope, app)
    tabs {
        tab("Scout") {
            column {
                viewGeoMap(app.geoMap, app.appScope)

                flowBlock(model.dataFlow, defaultMagic, magic = true) { data ->
                    val edit = data.edit; val location = data.location; val places = data.places
                    viewOf(model) {
                        if (location != null) {
                            finishedStage(location)
                        } else if (edit != null) {
                            reviewStage(edit)
                        } else if (places != null) {
                            choosePlaceStage(places)
                        } else {
                            findPointStage()
                        }
                    }
                }
            }
        }
        tab("Your locations") {
            textBlock("yer locations")
        }
        tab("More Info") {
            textBlock("yer info")
        }
    }
}

fun ViewContext<LocationScout>.findPointStage() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val limitCityFlow = model.stateFlow.mapDistinct { it.limitCity }
    val limitMapFlow = model.stateFlow.mapDistinct { it.limitMap }
    val limitStateFlow = model.stateFlow.mapDistinct { it.limitState }

    column {
        card(modify(AlignItemsEnd)) {
            messageBox(model.messageFlow)
            row(modify(JustifyEnd)) {
                textBlock("Move the map target to the location.", modify(Dim))
                button("Here", onClick = model::here)
            }
        }

        spacer("or")

        card {
            heading4("OpenStreetMap")
            textBlock("We can also search OpenStreetMap by the location's name, address, city, type of establishment, etc.", modify(Dim))

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
    }
}

fun ViewContext<LocationScout>.choosePlaceStage(places: List<Place>) {
    card {
        messageBox(model.messageFlow, modify(Flex1))
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
        button("start over", modify(Secondary), onClick = model::reset)
    }
}

fun ViewContext<LocationScout>.reviewStage(edit: LocationEdit) {
    column {
        card() {
            messageBox(model.messageFlow, modify(Flex1))
            row {
                textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.website })
                button("🤖 read link", onClick = model::readLink)
            }
        }

        tabs {
            tab("Details") {
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
                        propertyValue("calendar", edit.eventsLink ?: "[No calendar]")
                        propertyValue("about", "[No about]")
                    }
                }
            }
            tab("Edit") {
                textBlock("yer editor")
            }
        }

        row(modify(JustifySpaceBetween)) {
            button("start over", modify(Secondary), onClick = model::reset)
            button("create", modify(Accent), onClick = model::postLocation)
        }
    }
}

fun ViewContext<LocationScout>.finishedStage(location: Location) {
    val portal = model.app.portal

    card {
        row {
            messageBox(model.messageFlow, modify(Flex1))
            button("Post events", modify(Accent), onClick = {
                portal.go(ReadEventRoute(location))
            })
        }
        row {
            box(modify(Flex1))
            button("start over", modify(Secondary), onClick = model::reset)
        }
    }
}