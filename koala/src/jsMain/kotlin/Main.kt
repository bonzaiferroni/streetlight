import koala.core.findAndInitGeoMap
import koala.core.findAndInitLottie
import kotlinx.browser.document

fun main() {
    console.log("koala rawr!")
    val body = document.body ?: return
    findAndInitGeoMap(body)
    findAndInitLottie(body)
}