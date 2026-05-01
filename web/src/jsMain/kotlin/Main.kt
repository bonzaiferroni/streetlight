import koala.core.setGlobalFunctions
import koala.dom.globalFunExtended
import kotlinx.browser.window
import streetlight.web.ui.viewApp

fun main() {
    console.log("loading streetlight")
    window.setGlobalFunctions(globalFunExtended)
    viewApp()
}