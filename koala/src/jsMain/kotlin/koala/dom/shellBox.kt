package koala.dom

import initElement
import koala.css.*
import koala.html.Id
import koala.html.ShellBoxKey
import kotlinx.html.DIV
import web.dom.document
import web.html.HTMLDivElement

fun ViewScope.shellBox(
    id: Id,
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
    }
}

fun ViewScope.shellBoxWithMap(
    id: Id,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val element = shellBox(id, mod, block)
    element.onView {
        wireGeoMap(element)
    }
    return element
}