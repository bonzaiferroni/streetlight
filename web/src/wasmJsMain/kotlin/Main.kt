import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.button
import kotlinx.html.dom.append

fun main() {
    document.body?.clear()
    document.body?.append {
        val element = button {
            +"Hello streetlight"
        }
        element.addEventListener("click", {
            println("hello streetlight")
        })
    }
}