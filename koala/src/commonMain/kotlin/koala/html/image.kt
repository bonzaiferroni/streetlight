package koala.html

import koala.Image
import koala.SiteImage
import koala.Svg
import koala.css.*
import kotlinx.html.*

fun FlowOrInteractiveOrPhrasingContent.image(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    block: IMG.() -> Unit = {}
) {
    img {
        this.src = src ?: placeholder.path
        addModifiers(modifiers)
        block()
    }
}

fun FlowOrInteractiveOrPhrasingContent.image(
    id: Id,
    src: String? = null,
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
    image(svg.path, modifiers, placeholder, block)
}
