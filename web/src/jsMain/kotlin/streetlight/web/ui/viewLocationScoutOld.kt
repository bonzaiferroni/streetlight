package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.heading4
import koala.html.textProperty
import koala.html.centeredHeading
import koala.model.mapDistinct
import kotlinx.coroutines.flow.filterNotNull
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.web.model.*

fun RenderContext.viewLocationScoutOld(app: Streetlight) {
    val model = LocationScout(renderScope, app)
    val dataFlow = model.stateFlow.mapDistinct {
        LocationScoutData(
            places = it.places,
            hasEdit = it.edit != null,
            location = it.location
        )
    }
    tabs {
        tab("Scout") {
            column {
                viewGeoMap(app.geoMap, app.appScope)

                flowBlock(dataFlow, defaultMagic) { data ->
                    val edit = model.stateNow.edit; val location = data.location; val places = data.places
                    viewContextOf(model) {
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

private data class LocationScoutData(
    val places: List<Place>?,
    val hasEdit: Boolean,
    val location: Location?,
)

fun ViewContext<LocationScout>.findPointStage() {
    val queryFlow = model.stateFlow.mapDistinct { it.query }
    val websiteFlow = model.stateFlow.mapDistinct { it.website }
    val limitCityFlow = model.stateFlow.mapDistinct { it.limitCity }
    val limitMapFlow = model.stateFlow.mapDistinct { it.limitMap }
    val limitStateFlow = model.stateFlow.mapDistinct { it.limitState }

    column {
        card(modify(AlignItemsEnd)) {
            messageBox(model.messageFlow)
            row(modify(JustifyContentEnd)) {
                textBlock("Move the map target to the location.", modify(Dim))
                button("Here", onClick = model::here)
            }
        }

        centeredHeading("or")

        card {
            heading4("OpenStreetMap")
            textBlock("We can also search OpenStreetMap by the location's name, address, city, type of establishment, etc.", modify(Dim))

            row(modify(AlignItemsStart)) {
                column(modify(Flex1, AlignItemsEnd)) {
                    textField("search", modify(Width100P), model::setQuery, queryFlow, placeholder = "Search by name or address")
                    row(modify(JustifyContentEnd)) {
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
                button("Search", onClick = model::searchQuery)
            }
        }

        centeredHeading("or")

        card {
            heading4("Read Website")
            textBlock("We can try reading the website content for relevant information.", modify(Dim))

            row {
                textField("website", modify(Flex1), model::setWebsite, websiteFlow)
                button("Read", onClick = model::coldRead)
            }
        }
    }
}

fun ViewContext<LocationScout>.choosePlaceStage(places: List<Place>) {
    card {
        messageBox(model.messageFlow, modify(Flex1))
        places.forEach { place ->
            val name = place.name ?: return@forEach
            action(onClick = { model.choosePlace(place) }, modify(Width100P)) {
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
    val editFlow = model.stateFlow.mapDistinct { it.edit }.filterNotNull()

    column {
        card() {
            messageBox(model.messageFlow, modify(Flex1))
            row {
                textField("website", modify(Flex1), model::setWebsite, model.stateFlow.mapDistinct { it.website })
                button("🤖 read website", onClick = model::readWebsite)
            }
        }

        tabs {
            tab("Details") {
                flowBlock(editFlow) { edit ->
                    row(modify(AlignItemsStart)) {
                        val imageUrl = edit.imageUrl
                        if (imageUrl != null) {
                            image(imageUrl, modify(Flex1, Width100P))
                        } else {
                            box(modify(Flex1, PlaceItemsCenter)) {
                                textBlock("no image")
                            }
                        }
                        column(modify(Flex2)) {
                            heading3(edit.name ?: "[No name found]")
                            textBlock(edit.description ?: "[No description]")
                            textProperty("address", edit.address ?: "[No address]")
                            textProperty("website", edit.website ?: "[No website]")
                            textProperty("calendar", edit.eventsUrl ?: "[No calendar]")
                            textProperty("about", edit.aboutUrl ?: "[No about]")
                            textProperty("menu", edit.menuUrl ?: "[No menu]")
                        }
                    }
                }
            }
            tab("Edit") {
                viewLocationEditor(edit, model.app, editFlow) {
                    model.setEdit(it)
                }
            }
        }

        row(modify(JustifyContentSpaceBetween)) {
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
                // portal.go(OldEventScoutRoute(location))
            })
        }
        row {
            box(modify(Flex1))
            button("start over", modify(Secondary), onClick = model::reset)
        }
    }
}