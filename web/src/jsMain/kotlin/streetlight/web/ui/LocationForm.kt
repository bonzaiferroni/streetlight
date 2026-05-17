package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import streetlight.web.model.LocationEditor

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

private val imageInstructions = "This image will appear in the feed and at the top of the event page."

fun RenderContext.locationImageForm(model: LocationEditor) =
    imageFormSection(imageInstructions, model::setImageUrl, model.imageUrlFlow)

fun RenderContext.locationLinks(model: LocationEditor) = formSection("Links") {
    column {
        textField("website", modify(), model::setLink, model.websiteFlow)
        textField("calendar", modify(), model::setEventsLink, model.linksFlow)
    }
}