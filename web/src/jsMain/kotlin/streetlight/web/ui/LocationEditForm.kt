package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import streetlight.model.data.LocationProperty
import streetlight.web.model.LocationEditor
import streetlight.web.ui.formFiller

fun ViewScope.locationEditFormBody(model: LocationEditor) = column(BodyStyle.Column) {
    locationWebsiteForm(model)
    locationDetailsForm(model)
    locationImageForm(model)
    locationLinksForm(model)
}

fun ViewScope.locationDetailsForm(model: LocationEditor) = formCard("Location Details") {
    formRow {
        formSection("Name") {
            formTextField(model.nameField, "title", maxLength = 50)
                .flowValid(LocationProperty.Name, model.validityField, contentScope)
        }
        formSection("Address / City") {
            row {
                textField(model.addressField, "address", modify(Flex1))
                textField(model.cityField, "city", modify(Flex1))
            }
        }
    }

    formSection("Description") {
        styledMarkdownEditor(
            state = model.descriptionField,
            label = "description",
            mod = modify(MinHeight24)
        )
    }
}

fun ViewScope.locationImageForm(model: LocationEditor) =
    imageFormSection(
        instructions = "This image will appear at the top of the location page.",
        imageEditor = model.imageEditor
    )

fun ViewScope.locationLinksForm(model: LocationEditor) = formCard("Links") {
    formRow {
        formSection("Website") {
            textField(model.websiteField, "website")
            formText("This location's home on the web.")
        }
        formSection("Calendar") {
            textField(model.eventsUrlField, "calendar")
            formText("Does the location have an events page or calendar?")
        }
    }
}

fun ViewScope.locationWebsiteForm(model: LocationEditor) = formCard("Website") {
    formRow {
        formSection("Read Website") {
            textField(model.websiteField, "website")
            formBullets("Does this location have a website? We can read it to find certain details.", "Image", "Description", "Links")
            formSubmit("🤖 read website", onClick = model::readWebsite, model.websiteMessage)
        }
        formFiller(LottieFile.Ghost)
    }
}