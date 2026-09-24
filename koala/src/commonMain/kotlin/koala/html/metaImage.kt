package koala.html

import koala.Image
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** [image] with its description as alt text and its attribution, linked when it has a URL, over its bottom corner. */
fun FlowContent.metaImage(
    image: Image?,
    mod: Modifier? = null,
    config: DIV.() -> Unit = { }
) {
    box(mod) {
        config()
        image(image, mod = modify(ObjectFitCover, PlaceSelfStretch), alt = image?.description)
        image?.attribution?.let {
            navigationIfNotNull(image.attributionUrl?.value, modify(AlignSelfEnd, JustifySelfEnd, Padding(1))) {
                textBlock(it, TextSmall)
            }
        }
    }
}