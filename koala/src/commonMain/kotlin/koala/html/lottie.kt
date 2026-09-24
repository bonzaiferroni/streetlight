package koala.html

import koala.Lottie
import kotlinx.html.*
import koala.modifier.*

/** A square Lottie animation of [file], played by the browser. */
fun FlowContent.lottie(
    file: Lottie,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        configureLottie(file, mod, block)
    }
}

internal fun DIV.configureLottie(
    file: Lottie,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null
) {
    addModifiers(modify(LottieClass.Core, mod))
    setAttribute(Attribute.Lottie, file)
    block?.invoke(this)
}

object LottieClass {
    val Core = Class("lottie")
}

// language="CSS"
val LottieCss get() = """
${LottieClass.Core} {
    aspect-ratio: 1 / 1;
}
"""