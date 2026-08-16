package koala.html

import kampfire.model.ImageVariants
import kampfire.model.Url
import kampfire.model.largest
import kampfire.model.toHtmlSrcSet
import koala.Image
import koala.SiteImage
import koala.Svg
import koala.css.*
import kotlinx.html.*

fun FlowOrInteractiveOrPhrasingContent.image(
    src: Url? = null,
    mod: ModifierSet? = null,
    placeholder: Url = SiteImage.placeholderLg,
    alt: String? = null,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    img {
        configureImage(src ?: placeholder, null, mod, alt, lazy, block)
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    svg: Svg,
    mod: ModifierSet? = null,
    placeholder: Url = SiteImage.placeholderLg,
    alt: String? = null,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    image(svg.url, mod, placeholder, alt, lazy, block)
}

fun FlowOrInteractiveOrPhrasingContent.image(
    image: Image?,
    mod: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    alt: String? = null,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    val image = image ?: placeholder
    img {
        configureImage(null, image, mod, alt, lazy, block)
    }
}

fun IMG.configureImage(
    src: Url? = null,
    image: Image? = null,
    mod: ModifierSet?,
    alt: String? = null,
    lazy: Boolean,
    block: IMG.() -> Unit
) {
    this.src = (src ?: image?.variants.largest ?: SiteImage.placeholderLg).value
    alt?.let {
        this.alt = it
    }
    image?.variants?.let {
        configureSrcSet(it)
    }
    image?.aspect?.let {
        setStyle(Property.AspectRatio.to(it))
    }
    addModifiers(mod)
    if (lazy) {
        loading = ImgLoading.lazy
    }
    block()
}

fun IMG.configureSrcSet(
    images: ImageVariants
) {
    setAttribute(Attribute.SrcSet, images.toHtmlSrcSet())
    setAttribute(Attribute.Sizes, "auto")
}