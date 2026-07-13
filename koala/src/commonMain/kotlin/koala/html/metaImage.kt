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
    size: ImageSize = ImageSize.Large,
    mod: ModifierSet? = null,
) {
    val src = image?.getSizeOrNull(size) ?: image?.url ?: SiteImage.getPlaceholder(size)
    box(mod) {
        image(src, mod = modify(ObjectFitCover, PlaceSelfStretch), alt = image?.description) {
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