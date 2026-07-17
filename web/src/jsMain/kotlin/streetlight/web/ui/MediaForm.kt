package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.tap
import streetlight.web.model.MediaEditor

fun ViewScope.mediaForm(model: MediaEditor) {
    val titleFlow = model.editFlow.tap { it.title }
    val subtitleFlow = model.editFlow.tap { it.subtitle }
    val textFlow = model.editFlow.tap { it.text }

    row(modify()) {
        imageDrop(model.imageEditor.imageFlow, model.imageEditor::setImage, modify(Height24, Aspect3By2, BorderRadius1))
        column(modify(Flex1)) {
            textField("title", model::setTitle, titleFlow)
            textField("subtitle", model::setSubtitle, subtitleFlow )
            textEditor("content", onValue = model::setText, flow = textFlow)
        }
    }
}