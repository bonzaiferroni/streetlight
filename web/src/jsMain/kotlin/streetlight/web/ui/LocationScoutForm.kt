package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.LocationScout
import streetlight.web.model.SearchMode

fun ViewScope.locationScoutForm(model: LocationScout) = formSectionLegacy("Find a location") {

    tabs(
        indexField = model.modeField
    ) {
        tab(SearchMode.Search.name) {
            locationSearchForm(model)
        }
        tab(SearchMode.Map.name) {
            locationMapForm(model)
        }
    }
}

private fun ViewScope.locationSearchForm(model: LocationScout) = formCard {
    formPart(
        instructions = "Streetlight locations will appear as you type.",
        bullets = listOf("If you don't see the location in the list, you can search OpenStreetMap.")
    ) {
        row {
            textField(model.queryField, "search", modify(Flex1))
            textField(model.cityField, "city", modify(Width24))
        }
        formSubmitLegacy("Search OSM", model::queryOSM, messages = model.queryMessage)

        flowBlock(model.hasOsmLocationsField, modify(Height32, OverflowYAuto)) { hasOsmLocations ->
            when (hasOsmLocations) {
                true -> {
                    selectionBlock(model.osmLocationsField, model.mapLocationField) { location ->
                        searchItem(location.name, location.address, location.city)
                    }
                }
                else -> {
                    selectionBlock(model.queryLocationsField, model.locationField) { location ->
                        searchItem(location.name, location.address, location.city)
                    }
                }
            }
        }
    }
}

private fun ViewScope.searchItem(
    name: String?,
    address: String?,
    city: String?,
) = row {
    name?.let {
        textBlock(it, modify(Bold))
    }
    address?.let {
        textBlock(it)
    }
    city?.let {
        textBlock(it, modify(OpacityHigh))
    }
}

private fun ViewScope.locationMapForm(model: LocationScout) = formCard {
    formPart(
        instructions = "Move the map to the location you wish to create."
    ) {
        geoMapMount(mod = FormMod.GeoMap)
        row(modify(JustifyContentEnd, AlignItemsStart)) {
            messageBox(model.mapMessage)
            button("Here", onClick = model::stageLocationFromMap)
        }
    }
}