package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import streetlight.web.model.PostEditor

fun RenderScope.postForm(model: PostEditor) {
    val titleFlow = model.contentFlow.mapDistinct { it.title }
    val subtitleFlow = model.contentFlow.mapDistinct { it.subtitle }
    val textFlow = model.contentFlow.mapDistinct { it.text }
    val imageFlow = model.contentFlow.mapDistinct { it.imageRef }

    row(modify()) {
        imageDrop(imageFlow, model::setImageUrl, modify(Height24, Aspect3By2, BorderRadius1))
        column(modify(Flex1)) {
            textField("title", null, model::setTitle, titleFlow)
            textField("subtitle", null, model::setSubtitle, subtitleFlow )
            textEditor("content", onValue = model::setText, flow = textFlow)
        }
    }
}