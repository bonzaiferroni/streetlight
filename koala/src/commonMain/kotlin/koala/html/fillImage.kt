package koala.html

import koala.Image
import koala.SiteImage
import koala.css.Class
import koala.css.HeightAuto
import koala.css.ModifierSet
import koala.css.ObjectFitContain
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.img

fun FlowContent.fillImage(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: Image = SiteImage.placeholder,
    fillWidth: Boolean = true,
    block: (IMG.() -> Unit)? = null
) {
    val src = src ?: placeholder.path
    div {
        addModifiers(ImageWithBackdropKey.Class, modifiers)
        img {
            addModifiers(ImageWithBackdropKey.BackdropClass)
            this.src = src
        }
        div {
            addModifiers(ImageWithBackdropKey.ImageContainerClass)
            img {
                val mod = when(fillWidth) {
                    true -> modify(ImageWithBackdropKey.ImageClass, HeightAuto)
                    else -> modify(ImageWithBackdropKey.ImageClass, ObjectFitContain)
                }
                addModifiers(mod)
                this.src = src

                block?.invoke(this)
            }
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