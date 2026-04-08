package koala.html

import kampfire.model.Url
import koala.Image
import koala.SiteImage
import koala.Svg
import koala.css.*
import kotlinx.html.*

fun FlowOrInteractiveOrPhrasingContent.image(
    src: Url? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    block: IMG.() -> Unit = {}
) {
    img {
        this.src = (src ?: placeholder.url).value
        addModifiers(modifiers)
        block()
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    id: Id,
    src: Url? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    block: IMG.() -> Unit = {}
) {
    image(src, modifiers, placeholder) {
        this.id = id.identifier
        block()
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    svg: Svg,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    block: IMG.() -> Unit = {}
) {
    image(svg.url, modifiers, placeholder, block)
}
