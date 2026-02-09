import koala.css.buildKoalaStyles
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.style
import streetlight.web.viewApp

fun main() {
    console.log("loading styles")
    document.head?.append {
        val rules = buildKoalaStyles()
        style {
            +rules
        }
    }

    console.log("loading webdev")
    viewApp()
}