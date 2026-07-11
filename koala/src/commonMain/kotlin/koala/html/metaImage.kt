package koala.html

import kampfire.model.Url
import koala.Image
import koala.css.*
import kotlinx.html.FlowContent

fun FlowContent.metaImage(
    src: Url,
    image: Image?,
    mod: ModifierSet? = null,
) {
    box(mod) {
        image(src, alt = image?.description) {
            image?.aspectRatio?.let {
                setStyle(Property.AspectRatio.to(it))
            }
        }
        image?.attribution?.let {
            navigationIfNotNull(image.attributionUrl?.value, modify(AlignSelfEnd, JustifySelfEnd, Padding1)) {
                textBlock(it, modify(TextSmall))
            }
        }
    }
}