package koala.html

import kotlinx.html.*

fun HTML.head(
    title: String,
    block: HEAD.() -> Unit,
) {
    head {
        title { +title }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1" }
        coreStyles()
        coreScripts()
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js") { }
        link { href = "/www/icon/foxicon.ico"; rel = "icon"}
//        script {src = "webdev.js" }
        block()
    }
}

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/www/css/$style" } }
}

fun HEAD.coreStyles() {
    val styles = listOf(
        "reset.css",
//        "styles.css",
        "typography.css",
        "button.css",
//        "layout.css",
//        "utilities.css",
//        "animation.css",
        "tabs.css",
        "logo.css",
        "geoMap.css"
    )
    styles.forEach { style -> link { rel = "stylesheet"; href = "/www/core/css/$style" } }
}

fun FlowOrMetaDataContent.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/www/js/$script") {} }
}

fun HEAD.coreScripts() {
    val scripts = listOf(
        "utils.js",
        "tabs.js",
        "logo.js",
        "lottie.js",
        "compiled/web.js"
    )
    scripts.forEach { script -> script(src = corePath + script) {} }
}

val corePath = "/www/core/js/"