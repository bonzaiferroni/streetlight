package koala.html

import koala.css.Css
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.imageWithBackdrop(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String = SiteImage.placeholder,
    block: (IMG.() -> Unit)? = null
) {
    val src = src ?: placeholder
    div {
        setModifiers(ElementClass.imageWithBackdrop, modifiers)
        img {
            setModifiers(Css("image-with-backdrop__backdrop"))
            this.src = src
        }
        img {
            setModifiers(Css("image-with-backdrop__image"))
            this.src = src

            block?.invoke(this)
        }
    }
}