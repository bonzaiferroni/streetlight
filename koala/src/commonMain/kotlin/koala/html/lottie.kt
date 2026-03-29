package koala.html

import koala.Lottie
import kotlinx.html.*
import koala.css.*
import koala.css.addModifiers

fun FlowContent.lottie(
    file: Lottie,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        configureLottie(file, modifiers, block)
    }
}

internal fun DIV.configureLottie(
    file: Lottie,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    addModifiers(modify(LottieKey.Class, modifiers))
    setAttribute(Attribute.Lottie, file)
    block?.invoke(this)
}

object LottieKey {
    val Class = Class("lottie")
}

// language="CSS"
val LottieCss get() = """
.lottie,
.lottie > svg {
    width: 100%;
    height: 100%;
}
"""