package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.MediaEditor

fun ViewScope.mediaForm(model: MediaEditor) = formCard {
    formRow {
        formSection("title") {
            textField(model.titleField, "title")
            textField(model.subtitleField, "subtitle")
        }
        formSection("image") {
            imageDrop(model.imageEditor.imageField, modify(Aspect3By2, BorderRadius1))
        }
    }

    formSection("content") {
        markdownEditor(model.textField, "content", modify(MinHeight32))
    }
}