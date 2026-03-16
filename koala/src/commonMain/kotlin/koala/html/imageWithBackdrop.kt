package koala.html

import koala.css.Css
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.img

fun FlowContent.imageWithBackdrop(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String = SiteImage.placeholder,
    block: (IMG.() -> Unit)? = null
) {
    val src = src ?: placeholder
    box {
        applyModifiers(ElementClass.imageWithBackdrop, modifiers)
        img {
            applyModifiers(Css("image-with-backdrop__backdrop"))
            this.src = src
        }
        img {
            applyModifiers(Css("image-with-backdrop__image"))
            this.src = src

            block?.invoke(this)
        }
    }
}