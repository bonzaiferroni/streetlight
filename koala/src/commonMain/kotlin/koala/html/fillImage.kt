package koala.html

import kampfire.model.ImageSize
import kampfire.model.ImageVariants
import kampfire.model.Url
import kampfire.model.largest
import koala.Image
import koala.SiteImage
import koala.css.Class
import koala.css.HeightAuto
import koala.css.ModifierSet
import koala.css.ObjectFitContain
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.ImgLoading
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.fillImage(
    imageUrl: Url?,
    modifiers: ModifierSet? = null,
    fillWidth: Boolean = true,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val src = imageUrl ?: SiteImage.placeholderLg
    div {
        configureFillImage(
            src = src,
            variants = null,
            modifiers = modifiers,
            fillWidth = fillWidth,
            lazy = lazy,
            block = block
        )
    }
}

fun FlowContent.fillImageSrcSet(
    image: Image? = null,
    modifiers: ModifierSet? = null,
    fillWidth: Boolean = true,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val variants = (image ?: SiteImage.placeholder).variants
    div {
        configureFillImage(
            src = null,
            variants = variants,
            modifiers = modifiers,
            fillWidth = fillWidth,
            lazy = lazy,
            block = block
        )
    }
}

fun DIV.configureFillImage(
    src: Url?,
    variants: ImageVariants?,
    modifiers: ModifierSet?,
    fillWidth: Boolean,
    lazy: Boolean,
    block: IMG.() -> Unit
) {
    val src = src ?: variants.largest ?: SiteImage.placeholderLg
    addModifiers(ImageWithBackdropKey.Class, modifiers)
    img {
        addModifiers(ImageWithBackdropKey.BackdropClass)
        this.src = src.value
        variants?.let {
            configureSrcSet(variants)
        }
    }
    div {
        addModifiers(ImageWithBackdropKey.ImageContainerClass)
        img {
            val mod = when(fillWidth) {
                true -> modify(ImageWithBackdropKey.ImageClass, HeightAuto)
                else -> modify(ImageWithBackdropKey.ImageClass, ObjectFitContain)
            }
            addModifiers(mod)
            this.src = src.value
            variants?.let {
                configureSrcSet(variants)
            }
            if (lazy) {
                loading = ImgLoading.lazy
            }

            block()
        }
    }
}

object ImageWithBackdropKey {
    val Class = Class("fill-image")
    val BackdropClass = Class("fill-image__backdrop")
    val ImageContainerClass = Class("fill-image__image-container")
    val ImageClass = Class("fill-image__image")
}

// language="CSS"
val FillImageCss get() = """
${ImageWithBackdropKey.Class} {
    position: relative;
    overflow: hidden;
}

${ImageWithBackdropKey.BackdropClass},
${ImageWithBackdropKey.ImageContainerClass} {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
}

${ImageWithBackdropKey.ImageContainerClass} {
    display: flex;
    align-items: center;
    justify-content: center;
}

${ImageWithBackdropKey.BackdropClass} {
    object-fit: cover;
    object-position: center;
    transform: scale(1.2);
    filter: blur(24px);
    opacity: 0.5;
}

${ImageWithBackdropKey.ImageClass} {
    width: 100%;
    height: 100%;
    object-position: center;
}
"""