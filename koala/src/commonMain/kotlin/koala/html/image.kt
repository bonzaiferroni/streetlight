package koala.html

import kampfire.model.ImageVariants
import kampfire.model.Url
import kampfire.model.largest
import kampfire.model.toHtmlSrcSet
import koala.Image
import koala.SiteImage
import koala.Svg
import koala.modifier.*
import kotlinx.html.*

/** The image at [src], or [placeholder] when there is none. */
fun FlowOrInteractiveOrPhrasingContent.image(
    src: Url? = null,
    mod: Modifier? = null,
    placeholder: Url = SiteImage.placeholderLg,
    alt: String? = null,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    img {
        configureImage(src ?: placeholder, null, mod, alt, lazy, block)
    }
}

/** The SVG [svg] as an image in its own colors. */
fun FlowOrInteractiveOrPhrasingContent.image(
    svg: Svg,
    mod: Modifier? = null,
    placeholder: Url = SiteImage.placeholderLg,
    alt: String? = null,
    lazy: Boolean = true,
    block: IMG.() -> Unit = {}
) {
    image(svg.url, mod, placeholder, alt, lazy, block)
}

/** [image] with its source set and aspect ratio, or [placeholder] when there is none. */
fun FlowOrInteractiveOrPhrasingContent.image(
    image: Image?,
    mod: Modifier? = null,
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

/**
 * Configures this element as an image of [src], or of [image] with its source set and aspect ratio.
 *
 * A new image component configures its `img` with this.
 */
fun IMG.configureImage(
    src: Url? = null,
    image: Image? = null,
    mod: Modifier?,
    alt: String? = null,
    lazy: Boolean,
    block: IMG.() -> Unit
) {
    this.src = (src ?: image?.variants.largest ?: image?.url ?: SiteImage.placeholderLg).value
    alt?.let {
        this.alt = it
    }
    image?.variants?.let {
        configureSrcSet(it)
    }
    image?.aspect?.let {
        setStyle(Css.AspectRatio.of(it))
    }
    addModifiers(mod)
    if (lazy) {
        loading = ImgLoading.lazy
    }
    block()
}

/** Sets the `srcset` of [images] with `sizes="auto"`, so the browser picks the variant for the rendered width. */
fun IMG.configureSrcSet(
    images: ImageVariants
) {
    setAttribute(Attribute.SrcSet, images.toHtmlSrcSet())
    setAttribute(Attribute.Sizes, "auto")
}