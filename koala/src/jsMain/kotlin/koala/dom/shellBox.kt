package koala.dom

import initElement
import koala.modifier.*
import kotlinx.html.DIV
import web.dom.document
import web.html.HTMLElement
import web.timers.setTimeout

/**
 * Adopts the server-rendered shell when the document has one, and otherwise builds it with [block].
 *
 * An adopted shell fades out when the view is disposed. [block] runs only when there is no shell to adopt.
 */
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

/** A [shellBox] whose map is wired once it comes into view. */
fun ViewScope.shellBoxWithMap(
    block: DIV.() -> Unit
): HTMLElement {
    val element = shellBox(block)
    element.onView {
        wireGeoMap(element)
    }
    return element
}

/** Fades the element out and removes it after [milliseconds]. */
fun HTMLElement.fadeAndRemove(milliseconds: Int = MagicStyle.Interval) {
    modify(FadeOut)
    setTimeout({ remove() }, milliseconds)
}

/** Removes the element's children after [milliseconds]. */
fun HTMLElement.clearAfterInterval(milliseconds: Int = MagicStyle.Interval) {
    setTimeout({ clear() }, milliseconds)
}