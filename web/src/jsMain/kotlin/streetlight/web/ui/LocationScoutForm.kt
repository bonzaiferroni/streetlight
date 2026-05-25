package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.flow.map
import streetlight.web.model.LocationScout
import streetlight.web.model.SearchMode

fun RenderContext.locationScoutForm(model: LocationScout) = formSection("Find a location") {
    val indexFlow = model.modeFlow.map { it.name }

    tabs(
        tabFlow = indexFlow,
        onChangeTab = { model.setMode(SearchMode.valueOf(it)) },
        defaultTab = model.stateNow.mode.name
    ) {
        tab(SearchMode.Search.name) {
            locationSearchForm(model)
        }
        tab(SearchMode.Map.name) {
            locationMapForm(model)
        }
    }
}

private fun RenderContext.locationSearchForm(model: LocationScout) = formCard {
    formPart(
        instructions = "Streetlight locations will appear as you type.",
        bullets = listOf("If you don't see the location in the list, you can search OpenStreetMap.")
    ) {
        row {
            textField("search", modify(Flex1), model::setQuery, model.queryFlow)
            textField("city", modify(Width24), model::setCity, model.cityFlow)
        }
        formSubmit("Search OSM", model::queryOSM, messages = model.queryMessage)

        flowBlock(model.hasOsmLocations, modify(Height32, OverflowYAuto)) { hasOsmLocations ->
            when (hasOsmLocations) {
                true -> {
                    selectionBlock(model.osmLocationsFlow, model::stageLocation) { location ->
                        textBlock(location.label)
                    }
                }
                else -> {
                    selectionBlock(model.queryLocationsFlow, model::setLocation, model.locationFlow) { location ->
                        textBlock(location.label)
                    }
                }
            }
        }
    }
}

private fun RenderContext.locationMapForm(model: LocationScout) = formCard {
    formPart(
        instructions = "Move the map to the location you wish to create."
    ) {
        geoMapMount(geoMap, appScope, modifiers = FormMod.GeoMap)
        row(modify(JustifyContentEnd, AlignItemsStart)) {
            messageBox(model.mapMessage)
            button("Here", onClick = model::stageLocationFromMap)
        }
    }
}