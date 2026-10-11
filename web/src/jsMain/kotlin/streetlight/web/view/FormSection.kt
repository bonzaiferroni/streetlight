package streetlight.web.view

import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.layout.box
import koala.dom.component.image
import koala.dom.component.imageDrop
import koala.dom.component.setBlockLabel
import streetlight.web.model.ImageEditor
import streetlight.web.dom.layout.formBullets
import streetlight.web.dom.layout.formSection

fun ViewScope.imageFormSection(
    instructions: String,
    imageEditor: ImageEditor,
) = formSection("Image", AlignSelfCenter) {
    imageDrop(imageEditor.imageField) {
        box {
            image(it.url, modify(ObjectFitContain, MaxHeight(64), BorderRadius1))
        }
    }.setBlockLabel("image")
    formBullets(
        null,
        instructions,
        // "Ideally at least 1080 pixels wide and 512 pixels tall.",
        imageRequirements,
    )
}

private val imageRequirements = "Suitable for all audiences."