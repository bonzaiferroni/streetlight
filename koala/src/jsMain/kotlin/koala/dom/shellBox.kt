package koala.dom

import initElement
import koala.modifier.*
import kotlinx.html.DIV
import web.dom.document
import web.html.HTMLElement
import web.timers.setTimeout

fun ViewScope.shellBox(
    block: DIV.() -> Unit
): HTMLElement {
    val shell = document.getElementOrNullById(KoalaBody.ShellMount)
    return if (shell != null) {
        onDispose {
            shell.fadeAndRemove()
        }
        shell
    } else {
        console.log("generating shell")
        val element = box {
            block()
        }

        initElement(element)
        element
    }
}

fun ViewScope.shellBoxWithMap(
    block: DIV.() -> Unit
): HTMLElement {
    val element = shellBox(block)
    element.onView {
        wireGeoMap(element)
    }
    return element
}

fun HTMLElement.fadeAndRemove(milliseconds: Int = MagicStyle.Interval) {
    modify(FadeOut)
    setTimeout({ remove() }, milliseconds)
}

fun HTMLElement.clearAfterInterval(milliseconds: Int = MagicStyle.Interval) {
    setTimeout({ clear() }, milliseconds)
}