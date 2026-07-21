package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.MediaEditor

fun ViewScope.mediaForm(model: MediaEditor) {
    // val titleFlow = model.editFlow.dedup { it.title }
    // val subtitleFlow = model.editFlow.dedup { it.subtitle }
    // val textFlow = model.editFlow.dedup { it.text }

    row(modify()) {
        imageDrop(model.imageEditor.imageField, modify(Height24, Aspect3By2, BorderRadius1))
        column(modify(Flex1)) {
            textField(model.titleField, "title")
            textField(model.subtitleField, "subtitle")
            textEditor(model.textField, "content")
        }
    }
}