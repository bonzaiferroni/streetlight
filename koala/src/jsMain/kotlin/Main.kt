import koala.core.findAndInitGeoMap
import koala.core.queryAndInitLotties
import koala.core.findAndInitSwitches
import koala.dom.findAndInitTabs
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

// the purpose of this module is to provide some basic ui functionality while the larger scripts are inbound
// it has no references to kotlinx.html so it is considerably smaller
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