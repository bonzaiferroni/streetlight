package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import streetlight.web.model.MediaEditor
import koala.dom.component.*
import koala.dom.markdown.*
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection

context(model: MediaEditor)
fun ViewScope.mediaForm() = formCard {
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
        markdownEditor(model.textField, "content", MinHeight(32))
    }
}