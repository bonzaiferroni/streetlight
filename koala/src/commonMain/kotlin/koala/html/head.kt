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
        link { href = "/static/icon/foxicon.ico"; rel = "icon"}
        block()
    }
}

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/css/$style" } }
}

fun HEAD.coreStyles() {
    val styles = listOf(
        "reset.css",
        "styles.css",
        "typography.css",
        "button.css",
        "layout.css",
        "utilities.css",
        "animation.css",
        "tabs.css",
        "logo.css",
        "geoMap.css"
    )
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/core/css/$style" } }
}

fun HEAD.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/static/js/$script") {} }
}

fun HEAD.coreScripts() {
    val scripts = listOf(
        "utils.js",
        "tabs.js",
        "logo.js",
        "lottie.js",
        // "geoMap.js",
        "compiled/web.js"
    )
    scripts.forEach { script -> script(src = corePath + script) {} }
}

val corePath = "/static/core/js/"