package koala.html

import koala.LottieFile
import kotlinx.html.*
import koala.css.*
import koala.css.addModifiers

fun FlowContent.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        configureLottie(file, modifiers, block)
    }
}

internal fun DIV.configureLottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    addModifiers(modify(LottieKey.Class, modifiers))
    setAttribute(TagAttribute.lottie, file)
    block?.invoke(this)
}

object LottieKey {
    val Class = Css("lottie")
}