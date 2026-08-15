package koala.html

import kampfire.model.ImageSize
import kampfire.model.Url
import koala.Image
import koala.SiteImage
import koala.css.*
import koala.getVariantOrPlaceholder
import kotlinx.html.FlowContent

fun FlowContent.metaImage(
    image: Image?,
    mod: ModifierSet? = null,
) {
    box(mod) {
        image(image, mod = modify(ObjectFitCover, PlaceSelfStretch), alt = image?.description)
        image?.attribution?.let {
            navigationIfNotNull(image.attributionUrl?.value, modify(AlignSelfEnd, JustifySelfEnd, Padding1)) {
                textBlock(it, modify(TextSmall))
            }
        }
    }
}