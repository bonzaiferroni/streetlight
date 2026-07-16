package streetlight.web.ui

import kampfire.model.Url
import koala.Image
import koala.dom.AppScope
import koala.dom.box
import koala.dom.image
import koala.dom.imageDrop
import koala.dom.setBlockLabel
import koala.html.textBlock
import kotlinx.coroutines.flow.Flow
import streetlight.web.model.ImageEditor

fun AppScope.imageFormSection(
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
        imageDrop(imageEditor.imageFlow, imageEditor::setImage) {
            box {
                image(it.url)
            }
        }.setBlockLabel("image")
    }
}

private val imageRequirements = "Suitable for all audiences."