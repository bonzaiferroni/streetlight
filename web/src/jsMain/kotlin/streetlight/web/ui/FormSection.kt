package streetlight.web.ui

import kampfire.model.Url
import koala.dom.RenderContext
import koala.dom.box
import koala.dom.image
import koala.dom.imageDrop
import koala.dom.setBlockLabel
import koala.model.mapDistinct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import streetlight.web.model.GalaxyEditor

fun RenderContext.imageFormSection(
    instructions: String,
    onValue: (Url?) -> Unit,
    imageFlow: Flow<Url?>,
) = formSection("Image") {
    formPart(
        instructions = instructions,
        bullets = listOf(
            "Ideally at least 1024 pixels wide and 512 pixels tall.",
            imageRequirements,
        )
    ) {
        imageDrop(imageFlow, onValue) {
            box {
                image(it)
            }
        }.setBlockLabel("image")
    }
}

private val imageRequirements = "Suitable for all audiences."