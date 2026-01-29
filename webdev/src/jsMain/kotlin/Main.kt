import koala.css.KoalaTheme
import koala.css.buildKoalaStyles
import koala.css.rules
import kotlinx.browser.document
import kotlinx.css.CssBuilder
import kotlinx.html.dom.append
import kotlinx.html.html
import kotlinx.html.js.style
import kotlinx.html.stream.appendHTML
import org.w3c.dom.HTMLIFrameElement
import streetlight.web.pages.homePage
import streetlight.web.viewApp

fun main() {
    console.log("loading styles")
    document.head?.append {
        val rules = buildKoalaStyles()
        console.log(rules)
        style {
            +rules
        }
    }

    console.log("loading webdev")
    viewApp()
}