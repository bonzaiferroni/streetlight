package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import streetlight.model.data.CityProperty
import streetlight.web.model.CityEditor
import koala.dom.component.*
import koala.dom.markdown.*
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formTextField

context(model: CityEditor)
fun ViewScope.cityDetailsForm() = formCard("City Details") {
    formRow {
        formSection("Name") {
            formTextField(model.nameField, "name", maxLength = 50)
                .flowValid(CityProperty.Name, model.validityField, contentScope)
        }
        context(model.imageEditor) {
            imageFormSection("This image will appear at the top of the city page.")
        }
    }

    formSection("Description") {
        markdownEditor(
            state = model.descriptionField,
            label = "description",
            mod = MinHeight(24),
        )
    }
}

context(model: CityEditor)
fun ViewScope.cityLinksForm() = formCard("City Links") {
    linksFormSection(model.linksState)
}
