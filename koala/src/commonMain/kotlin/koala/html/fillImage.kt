package koala.html

import kampfire.model.ScaledImageArray
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
    src: Url? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholderLg,
    fillWidth: Boolean = true,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val src = src ?: placeholder.url
    div {
        configureFillImage(
            src = src,
            images = null,
            modifiers = modifiers,
            fillWidth = fillWidth,
            lazy = lazy,
            block = block
        )
    }
}

fun FlowContent.fillImage(
    images: ScaledImageArray? = null,
    modifiers: ModifierSet? = null,
    placeholder: ScaledImageArray = SiteImage.placeholder,
    fillWidth: Boolean = true,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val images = images ?: placeholder
    div {
        configureFillImage(
            src = null,
            images = images,
            modifiers = modifiers,
            fillWidth = fillWidth,
            lazy = lazy,
            block = block
        )
    }
}

fun DIV.configureFillImage(
    src: Url?,
    images: ScaledImageArray?,
    modifiers: ModifierSet?,
    fillWidth: Boolean,
    lazy: Boolean,
    block: IMG.() -> Unit
) {
    val src = src ?: images.largest ?: SiteImage.placeholderLg.url
    addModifiers(ImageWithBackdropKey.Class, modifiers)
    img {
        addModifiers(ImageWithBackdropKey.BackdropClass)
        this.src = src.value
        images?.let {
            configureImages(images)
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
            images?.let {
                configureImages(images)
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