package streetlight.web.ui

import koala.modifier.*
import koala.dom.*
import streetlight.model.data.CityProperty
import streetlight.web.model.CityEditor

fun ViewScope.cityDetailsForm(model: CityEditor) = formCard("City Details") {
    formRow {
        formSection("Name") {
            formTextField(model.nameField, "name", maxLength = 50)
                .flowValid(CityProperty.Name, model.validityField, contentScope)
        }
        imageFormSection(
            instructions = "This image will appear at the top of the city page.",
            imageEditor = model.imageEditor,
        )
    }

    formSection("Description") {
        markdownEditor(
            state = model.descriptionField,
            label = "description",
            mod = MinHeight(24),
        )
    }
}

fun ViewScope.cityLinksForm(model: CityEditor) = formCard("City Links") {
    linksFormSection(model.linksState)
}
