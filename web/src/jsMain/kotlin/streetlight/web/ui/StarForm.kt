package streetlight.web.ui

import koala.dom.*
import streetlight.web.model.AccountEditor
import streetlight.web.model.ProfileEditor

fun ViewScope.starProfileForm(model: ProfileEditor) = form {
    formRow {
        imageFormSection(imageInstructions1, model.imageEditor)
        formSection("Content") {
            textField(model.taglineField, "tagline", maxLength = 50)
            formText("The tagline will appear under your name.")
            textEditor(model.descriptionField, "description")
            formText("The description will appear under the image, before your posts.")
        }
    }
    formSubmit("Save", model::submit, model.messages)
}

private val imageInstructions1 = "This image will appear at the top of your profile."



