package streetlight.web.ui

import koala.modifier.AlignSelfCenter
import koala.modifier.BorderRadius1
import koala.modifier.MaxHeight64
import koala.modifier.ObjectFitContain
import koala.modifier.modify
import koala.dom.ViewScope
import koala.dom.box
import koala.dom.image
import koala.dom.imageDrop
import koala.dom.setBlockLabel
import streetlight.web.model.ImageEditor

fun ViewScope.imageFormSection(
    instructions: String,
    imageEditor: ImageEditor,
) = formSection("Image", modify(AlignSelfCenter)) {
    imageDrop(imageEditor.imageField) {
        box {
            image(it.url, modify(ObjectFitContain, MaxHeight64, BorderRadius1))
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