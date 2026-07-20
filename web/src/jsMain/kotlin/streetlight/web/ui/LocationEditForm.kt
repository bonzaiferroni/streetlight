package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.LocationProperty
import streetlight.web.model.LocationEditor

fun ViewScope.locationEditFormBody(model: LocationEditor) = formBodyProto {
    locationWebsiteForm(model)
    locationDetailsForm(model)
    locationImageForm(model)
    locationLinksForm(model)
}

fun ViewScope.locationDetailsForm(model: LocationEditor) = formCardSection("Location Details") {
    formPart("What is the name of the place?") {
        formTextField("title", model::setName, model.nameFlow, maxLength = 50)
            .flowValid(LocationProperty.Name, model.validityFlow, contentScope)
    }
    formPart("Where is it?") {
        row {
            textField("address", model::setAddress, model.addressFlow, modify(Flex1))
            textField("city", model::setCity, model.cityFlow, modify(Flex1))
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

fun ViewScope.locationImageForm(model: LocationEditor) =
    imageFormSection(
        instructions = "This image will appear at the top of the location page.",
        imageEditor = model.imageEditor
    )

fun ViewScope.locationLinksForm(model: LocationEditor) = formCardSection("Links") {
    column {
        textField("calendar", model::setEventsLink, model.linksFlow)
    }
}

fun ViewScope.locationWebsiteForm(model: LocationEditor) = formCardSection("Website") {
    formPart(
        instructions = "Does this location have a website? We can read it to find certain details.",
        bullets = listOf("Image", "Description", "Links")
    ) {
        textField("website", model::setWebsite, model.websiteFlow)
        row(modify(JustifyContentEnd)) {
            messageBox(model.websiteMessage, modify(Magic))
            button("🤖 read website", onClick = model::readWebsite)
                .flowIsWorking(model.websiteMessage.isWorkingFlow, contentScope)
        }
    }
}