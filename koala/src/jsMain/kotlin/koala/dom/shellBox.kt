package koala.dom

import initElement
import koala.css.*
import koala.html.Id
import koala.html.ShellBoxKey
import kotlinx.browser.document
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun AppScope.shellBox(
    id: Id,
    initializers: List<AppScope.(HTMLElement) -> Unit>,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val shell = document.getElementOrNullById(id)
    return if (shell != null) {
        console.log("grabbing shell: $id")
        val element = container {
            addModifiers(ShellBoxKey.Class, mod)
        }

        element.append(shell)
        element
    } else {
        console.log("generating shell: $id")
        val element = container {
            addModifiers(ShellBoxKey.Class, mod)
            block()
        }

        initElement(element)
        element
    }.also { element ->
        initializers.forEach {
            it(element)
        }
    }
}

fun AppScope.shellBoxWithMap(
    id: Id,
    initializers: List<AppScope.(HTMLElement) -> Unit>,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val element = shellBox(id, initializers, mod, block)
    element.onView {
        wireGeoMap(element)
    }
    return element
}