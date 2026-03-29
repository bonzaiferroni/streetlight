package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.fillImage(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String = SiteImage.placeholder,
    block: (IMG.() -> Unit)? = null
) {
    val src = src ?: placeholder
    div {
        addModifiers(ImageWithBackdropKey.Class, modifiers)
        img {
            addModifiers(Class("image-with-backdrop__backdrop"))
            this.src = src
        }
        img {
            addModifiers(Class("image-with-backdrop__image"))
            this.src = src

            block?.invoke(this)
        }
    }
}

object ImageWithBackdropKey {
    val Class = Class("image-with-backdrop")
}

// language="CSS"
val FillImageCss get() = """
.image-with-backdrop {
    position: relative;
    overflow: hidden;
}

.image-with-backdrop__backdrop,
.image-with-backdrop__image {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
}

.image-with-backdrop__backdrop {
    object-fit: cover;
    object-position: center;
    transform: scale(1.2);
    filter: blur(24px);
    opacity: 0.5;
}

.image-with-backdrop__image {
    object-fit: contain;
    object-position: center;
}
"""