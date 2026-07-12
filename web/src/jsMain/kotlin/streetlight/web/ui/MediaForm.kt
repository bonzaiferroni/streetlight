package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import streetlight.web.model.MediaEditor

fun AppScope.mediaForm(model: MediaEditor) {
    val titleFlow = model.editFlow.mapDistinct { it.title }
    val subtitleFlow = model.editFlow.mapDistinct { it.subtitle }
    val textFlow = model.editFlow.mapDistinct { it.text }

    row(modify()) {
        imageDrop(model.imageEditor.imageFlow, model.imageEditor::setImage, modify(Height24, Aspect3By2, BorderRadius1))
        column(modify(Flex1)) {
            textField("title", model::setTitle, titleFlow)
            textField("subtitle", model::setSubtitle, subtitleFlow )
            textEditor("content", onValue = model::setText, flow = textFlow)
        }
    }
}