package koala.dom

import koala.core.findAndInitGeoMap
import koala.core.findAndInitLottie
import koala.css.*
import koala.html.Id
import koala.model.GeoMap
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

fun RenderContext.shellBox(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val shell = document.getElementOrNullById(id)
    return if (shell != null) {
        console.log("grabbing shell: $id")
        val element = container {
            applyModifiers(ElementClass.shellBox, modifiers)
        }

        element.append(shell)
        element
    } else {
        console.log("generating shell: $id")
        val element = container {
            applyModifiers(ElementClass.shellBox, modifiers)
            block()
        }

        findAndInitTabs(element)
        findAndInitLottie(element)
        findAndInitGeoMap(element)
        element
    }
}

fun RenderContext.shellBox(
    id: Id,
    geoMap: GeoMap,
    appScope: CoroutineScope,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val element = shellBox(id, modifiers, block)
    element.onView {
        wireGeoMap(geoMap, appScope, element)
    }
    return element
}