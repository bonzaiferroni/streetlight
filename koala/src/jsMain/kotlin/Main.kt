import koala.core.findAndInitGeoMap
import koala.core.queryAndInitLotties
import koala.interop.interopUtilities
import koala.dom.findAndInitTabs
import koala.interop.addGlobalFunctions
import web.dom.document
import web.html.HTMLElement

// the purpose of this module is to provide some basic ui functionality while the larger scripts are inbound
// it has no references to kotlinx.html so it is considerably smaller
fun main() {
    console.log("koala rawr!!")
    addGlobalFunctions(interopUtilities)
    initElement(document.body)
}

fun initElement(element: HTMLElement) {
    findAndInitGeoMap(element)
    queryAndInitLotties(element)
    findAndInitTabs(element)
}