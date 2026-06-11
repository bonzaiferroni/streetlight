package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.LocationProperty
import streetlight.web.model.LocationEditor

fun DOMRender.locationEditFormBody(model: LocationEditor) = formBody {
    locationWebsiteForm(model)
    locationDetailsForm(model)
    locationImageForm(model)
    locationLinksForm(model)
}

fun DOMRender.locationDetailsForm(model: LocationEditor) = formCardSection("Location Details") {
    formPart("What is the name of the place?") {
        formTextField("title", model::setName, model.nameFlow, maxLength = 50)
            .flowValid(LocationProperty.Name, model.validityFlow, renderScope)
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

fun DOMRender.locationImageForm(model: LocationEditor) =
    imageFormSection(
        instructions = "This image will appear at the top of the location page.",
        onValue = model::setImageUrl,
        imageFlow = model.imageUrlFlow
    )

fun DOMRender.locationLinksForm(model: LocationEditor) = formCardSection("Links") {
    column {
        textField("calendar", modify(), model::setEventsLink, model.linksFlow)
    }
}

fun DOMRender.locationWebsiteForm(model: LocationEditor) = formCardSection("Website") {
    formPart(
        instructions = "Does this location have a website? We can read it to find certain details.",
        bullets = listOf("Image", "Description", "Links")
    ) {
        textField("website", modify(), model::setWebsite, model.websiteFlow)
        row(modify(JustifyContentEnd)) {
            messageBox(model.websiteMessage, modify(Magic))
            button("🤖 read website", onClick = model::readWebsite)
                .flowIsWorking(model.websiteMessage.isWorkingFlow, renderScope)
        }
    }
}