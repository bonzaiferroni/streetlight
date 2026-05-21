package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.flow.map
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScout
import streetlight.web.model.LocationScoutMode

fun RenderContext.locationDetailsForm(model: LocationEditor) = formCardSection("Location Details") {
    formPart("What is the name of the place?") {
        formTextField("title", model::setName, model.nameFlow, maxLength = 50)
    }
    formPart("Where is it?") {
        row {
            textField("address", modify(Flex1), model::setAddress, model.addressFlow)
            textField("city", modify(Flex1), model::setCity, model.cityFlow)
        }
    }
    formPart("Describe the place.", fieldsFlex = Flex2) {
        textEditor(
            label = "description",
            rows = 8,
            onValue = model::setDescription,
            flow = model.descriptionFlow
        )
    }
}

private val imageInstructions = "This image will appear at the top of the location page."

fun RenderContext.locationImageForm(model: LocationEditor) =
    imageFormSection(imageInstructions, model::setImageUrl, model.imageUrlFlow)

fun RenderContext.locationLinksForm(model: LocationEditor) = formCardSection("Links") {
    column {
        textField("calendar", modify(), model::setEventsLink, model.linksFlow)
    }
}

fun RenderContext.locationWebsiteForm(model: LocationEditor) = formCardSection("Website") {
    formPart(
        instructions = "Does this location have a website? We can read it to find certain details.",
        bullets = listOf("Image", "Description", "Links")
    ) {
        textField("website", modify(), model::setWebsite, model.websiteFlow)
        row(modify(JustifyContentEnd)) {
            messageBox(model.websiteMsg, modify(Magic))
            button("🤖 read website", onClick = model::readWebsite)
        }
    }
}

fun RenderContext.locationScoutForm(model: LocationScout) = formSection("Find a location") {
    val indexFlow = model.modeFlow.map { it.name }

    tabs(
        tabFlow = indexFlow,
        onChangeTab = { model.setMode(LocationScoutMode.valueOf(it)) },
        defaultTab = model.stateNow.mode.name
    ) {
        tab(LocationScoutMode.Search.name) {
            locationSearchForm(model)
        }
        tab(LocationScoutMode.Map.name) {
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
        row(modify(JustifyContentEnd)) {
            // textField("state", modify(Flex1))
            button("Search OSM", onClick = model::queryOSM)
        }
        flowBlock(model.hasOsmLocations, modify(Height32, OverflowYAuto)) { hasOsmLocations ->
            when (hasOsmLocations) {
                true -> {
                    selectionBlock(model.osmLocationsFlow, model::stageLocation) { location ->
                        textBlock(location.displayTitle)
                    }
                }
                else -> {
                    selectionBlock(model.locationsFlow, model::setLocation, model.locationFlow) { location ->
                        textBlock(location.displayTitle)
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
            messageBox(model.mapMsg)
            button("Here", onClick = model::stageLocationFromMap)
        }
    }
}