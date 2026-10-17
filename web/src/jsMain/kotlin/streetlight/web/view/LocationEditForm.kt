package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import streetlight.model.data.LocationProperty
import streetlight.web.model.LocationEditor
import koala.dom.component.*
import koala.dom.markdown.*
import streetlight.web.layout.BodyStyle
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.dom.layout.formTextField

context(model: LocationEditor)
fun ViewScope.locationEditFormBody() = column(BodyStyle.MainColumn) {
    locationDetailsForm()
    locationImageForm()
    locationLinksForm()
}

context(model: LocationEditor)
fun ViewScope.locationDetailsForm() = formCard("Location Details") {
    formRow {
        formSection("Name") {
            formTextField(model.nameField, "title", maxLength = 50)
                .flowValid(LocationProperty.Name, model.validityField, contentScope)
        }
        formSection("Address / City") {
            row {
                textField(model.addressField, "address", Flex1)
                textField(model.cityField, "city", Flex1)
            }
        }
    }

    formSection("Description") {
        markdownEditor(
            state = model.descriptionField,
            label = "description",
            mod = MinHeight(24)
        )
    }
}

context(model: LocationEditor)
fun ViewScope.locationImageForm() = context(model.imageEditor) {
    imageFormSection("This image will appear at the top of the location page.")
}

context(model: LocationEditor)
fun ViewScope.locationLinksForm() = formCard("Links") {
    formRow {
        formSection("Website") {
            textField(model.websiteField, "website")
            centeredText("This location's home on the web.")
        }
        formSection("Calendar") {
            textField(model.eventsUrlField, "calendar")
            centeredText("Does the location have an events page or calendar?")
        }
    }
}

context(model: LocationEditor)
fun ViewScope.locationHostForm() = formCard("Host") {
    formSection {
        checkBox(model.isHostField, "I am the owner of this location or have permission to manage their profile on Streetlight.")
    }
}
