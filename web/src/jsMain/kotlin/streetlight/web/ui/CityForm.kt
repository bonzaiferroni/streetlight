package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.CityProperty
import streetlight.web.model.CityEditor

fun ViewScope.cityDetailsForm(model: CityEditor) = formCard("City Details") {
    formSection("Name") {
        formTextField(model.nameField, "name", maxLength = 50)
            .flowValid(CityProperty.Name, model.validityField, contentScope)
    }
}

fun ViewScope.cityImageForm(model: CityEditor) =
    imageFormSection(
        instructions = "This image will appear at the top of the city page.",
        imageEditor = model.imageEditor,
    )
