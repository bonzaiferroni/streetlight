package koala.html

import kampfire.model.ImageSize
import kampfire.model.large
import koala.Image
import koala.SiteImage
import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.featureImage(
    image: Image? = null,
    modifiers: ModifierSet? = null,
    size: ImageSize = ImageSize.Medium,
    block: DIV.() -> Unit = {}
) {
    val src = image?.getVariantOrNull(size) ?: SiteImage.getPlaceholder(size)
    div {
        addModifiers(Class, modifiers)
        block()

        img {
            addModifiers(BackdropClass)
            this.src = src.value
        }

        img {
            addModifiers(ContentClass)
            this.src = src.value
        }
    }
}

private val Class = Class("feature-image")
private val BackdropClass = Class("feature-image__backdrop")
private val ContentClass = Class("feature-image__content")

// language="CSS"
val FeatureImageCss get() = """
$Class {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: clip;
    min-width: 0;
    object-fit: contain;
}

$BackdropClass {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: fill;
    transform: scale(1.2);
    filter: blur(24px) brightness(0.8);
}

$ContentClass {
    height: 100%;
    position: relative;
    min-width: 0;
    min-height: 0;
    object-fit: inherit;
}
"""