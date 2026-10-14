package streetlight.web.view

import koala.dom.*
import streetlight.web.model.ProfileEditor
import koala.dom.component.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection

context(model: ProfileEditor)
fun ViewScope.starProfileForm() = formColumn {
    formRow {
        context(model.imageEditor) {
            imageFormSection(imageInstructions1)
        }
        formSection("Content") {
            textField(model.taglineField, "tagline", maxLength = 50)
            centeredText("The tagline will appear under your name.")
            textEditor(model.descriptionField, "description")
            centeredText("The description will appear under the image, before your posts.")
        }
    }
}

private val imageInstructions1 = "This image will appear at the top of your profile."



