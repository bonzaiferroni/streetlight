package koala.dom

import initElement
import koala.css.*
import koala.html.Id
import koala.html.ShellBoxKey
import koala.model.GeoCamera
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement

fun RenderContext.shellBox(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val shell = document.getElementOrNullById(id)
    return if (shell != null) {
        console.log("grabbing shell: $id")
        val element = container {
            addModifiers(ShellBoxKey.Class, modifiers)
        }

        element.append(shell)
        element
    } else {
        console.log("generating shell: $id")
        val element = container {
            addModifiers(ShellBoxKey.Class, modifiers)
            block()
        }

        initElement(element)
        element
    }
}

fun RenderContext.shellBoxWithMap(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val element = shellBox(id, modifiers, block)
    element.onView {
        wireGeoMap(element)
    }
    return element
}