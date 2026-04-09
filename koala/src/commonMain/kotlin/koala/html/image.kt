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
    block: IMG.() -> Unit = {}
) {
    img {
        this.src = (src ?: placeholder.url).value
        addModifiers(modifiers)
        block()
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    svg: Svg,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholderLg,
    block: IMG.() -> Unit = {}
) {
    image(svg.url, modifiers, placeholder, block)
}

fun FlowOrInteractiveOrPhrasingContent.image(
    images: ScaledImageArray?,
    modifiers: ModifierSet? = null,
    placeholder: ScaledImageArray = SiteImage.placeholder,
    block: IMG.() -> Unit = {}
) {
    val images = images ?: placeholder
    img {
        src = (images.largest ?: SiteImage.placeholderLg.url).value
        configureImages(images)
        addModifiers(modifiers)
        block()
    }
}

fun IMG.configureImages(
    images: ScaledImageArray
) {
    setAttribute(Attribute.SrcSet, images.toHtmlSrcSet())
    setAttribute(Attribute.Sizes, images.toHtmlSizes())
}