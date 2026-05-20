package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScout

fun RenderContext.locationDetailsForm(model: LocationEditor) = formSection("Location Details") {
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

fun RenderContext.locationLinksForm(model: LocationEditor) = formSection("Links") {
    column {
        textField("calendar", modify(), model::setEventsLink, model.linksFlow)
    }
}

fun RenderContext.locationWebsiteForm(model: LocationEditor) = formSection("Website") {
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

fun RenderContext.locationFinderForm(model: LocationScout) = formSection("Find a location") {
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
                    selectionBlock(model.osmLocationsFlow, model::setOSMLocation) { location ->
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