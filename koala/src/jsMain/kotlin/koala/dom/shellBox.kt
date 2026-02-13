package koala.dom

import koala.css.*
import koala.html.Id
import kotlinx.browser.document
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
        val element = box {
            applyModifiers(modifiers)
        }

        element.append(shell)
        element
    } else {
        console.log("generating shell: $id")
        val element = box {
            applyModifiers(modifiers)
            block()
        }

        findAndInitTabs(element)
        element
    }
}