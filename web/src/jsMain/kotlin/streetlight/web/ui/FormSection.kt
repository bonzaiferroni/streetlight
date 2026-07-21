package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.box
import koala.dom.image
import koala.dom.imageDrop
import koala.dom.setBlockLabel
import koala.html.textBlock
import streetlight.web.model.ImageEditor

fun ViewScope.imageFormSection(
    instructions: String,
    imageEditor: ImageEditor,
) = formSection("Image") {
    textBlock(instructions)
    formBullets(
        null,
        "Ideally at least 1024 pixels wide and 512 pixels tall.",
        imageRequirements,
    )
    formPart(
        instructions = instructions,
        bullets = listOf(

        )
    ) {
        imageDrop(imageEditor.imageField) {
            box {
                image(it.url)
            }
        }.setBlockLabel("image")
    }
}

private val imageRequirements = "Suitable for all audiences."