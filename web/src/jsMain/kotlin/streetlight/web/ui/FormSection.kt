package streetlight.web.ui

import kampfire.model.Url
import koala.Image
import koala.dom.AppScope
import koala.dom.box
import koala.dom.image
import koala.dom.imageDrop
import koala.dom.setBlockLabel
import kotlinx.coroutines.flow.Flow

fun AppScope.imageFormSection(
    instructions: String,
    onValue: (Image?) -> Unit,
    imageFlow: Flow<Image?>,
) = formCardSection("Image") {
    formPart(
        instructions = instructions,
        bullets = listOf(
            "Ideally at least 1024 pixels wide and 512 pixels tall.",
            imageRequirements,
        )
    ) {
        imageDrop(imageFlow, onValue) {
            box {
                image(it.url)
            }
        }.setBlockLabel("image")
    }
}

private val imageRequirements = "Suitable for all audiences."