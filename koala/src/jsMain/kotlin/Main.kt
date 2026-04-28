import koala.core.findAndInitGeoMap
import koala.core.queryAndInitLotties
import koala.core.findAndInitSwitches
import koala.css.Reveal
import koala.dom.findAndInitTabs
import koala.dom.modify
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun main() {
    console.log("koala rawr!!")
    val body = document.body ?: return

    initElement(body)
}

fun initElement(element: HTMLElement) {
    findAndInitGeoMap(element)
    queryAndInitLotties(element)
    findAndInitSwitches(element)
    findAndInitTabs(element)
}