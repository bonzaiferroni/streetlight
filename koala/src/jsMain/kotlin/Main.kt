import koala.core.findAndInitGeoMap
import koala.core.queryAndInitLotties
import koala.core.findAndInitSwitches
import koala.css.Reveal
import koala.dom.findAndInitTabs
import koala.dom.modify
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

fun main() {
    console.log("koala rawr!!")
    val body = document.body ?: return

    val hash = window.location.hash.takeIf { it.isNotEmpty() }
    if (hash == null) {
        // revealContent()
    }

    initElement(body)
}

fun initElement(element: HTMLElement) {
    findAndInitGeoMap(element)
    queryAndInitLotties(element)
    findAndInitSwitches(element)
    findAndInitTabs(element)
}

fun revealContent() {
    val element = document.getElementById("content-box") as? HTMLElement ?: error("content-box not found")
    element.modify(Reveal)
}