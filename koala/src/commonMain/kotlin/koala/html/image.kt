package koala.html

import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.largest
import kampfire.model.toHtmlSizes
import kampfire.model.toHtmlSrcSet
import koala.Image
import koala.SiteImage
import koala.Svg
import koala.css.*
import kotlinx.html.*

fun FlowOrInteractiveOrPhrasingContent.image(
    src: Url? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholderLg,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    img {
        configureImage(src ?: placeholder.url, null, modifiers, lazy, block)
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    svg: Svg,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholderLg,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    image(svg.url, modifiers, placeholder, lazy, block)
}

fun FlowOrInteractiveOrPhrasingContent.image(
    images: ScaledImageArray?,
    modifiers: ModifierSet? = null,
    placeholder: ScaledImageArray = SiteImage.placeholder,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val images = images ?: placeholder
    img {
        configureImage(null, images, modifiers, lazy, block)
    }
}

fun IMG.configureImage(
    src: Url? = null,
    images: ScaledImageArray? = null,
    modifiers: ModifierSet?,
    lazy: Boolean,
    block: IMG.() -> Unit
) {
    this.src = (src ?: images.largest ?: SiteImage.placeholderLg.url).value
    images?.let {
        configureImages(it)
    }
    addModifiers(modifiers)
    if (lazy) {
        loading = ImgLoading.lazy
    }
    block()
}

fun IMG.configureImages(
    images: ScaledImageArray
) {
    setAttribute(Attribute.SrcSet, images.toHtmlSrcSet())
    setAttribute(Attribute.Sizes, "auto")
}