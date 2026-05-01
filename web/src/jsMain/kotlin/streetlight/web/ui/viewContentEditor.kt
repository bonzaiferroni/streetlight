package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import streetlight.web.model.ContentEditor

fun ViewContext<ContentEditor>.viewContentEditor() {
    val titleFlow = model.contentFlow.mapDistinct { it.title }
    val textFlow = model.contentFlow.mapDistinct { it.text }
    val imageFlow = model.contentFlow.mapDistinct { it.imageRef }

    column {
        row(modify(Height24)) {
            imageDrop(imageFlow, model::setImage, modify(Aspect3By2, BorderRadius1))
            textField("title", modify(Flex1), model::setTitle, titleFlow)
        }
        textEditor("content", onValue = model::setText, flow = textFlow)
    }
}