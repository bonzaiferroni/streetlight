package koala.html

import koala.css.koalaStyles
import kotlinx.html.*

fun HTML.head(
    title: String,
    block: HEAD.() -> Unit,
) {
    head {
        title { +title }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1" }
        link { href = "/www/icon/foxicon.ico"; rel = "icon"}
        block()
        applyCoreStyles()
        applyCoreScripts()
        koalaStyles()
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js") { }
    }
}

fun HEAD.applyStyles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = cssPath + style } }
}

fun HEAD.applyCoreStyles() {
    applyStyles(
        "reset.css",
        "styles.css",
        "typography.css",
        "button.css",
        "layout.css",
        "animation.css",
        "tabs.css",
        "logo.css",
        "geoMap.css",
        "sandbox.css",
        "elements.css",
        "utilities.css",
    )
}

fun FlowOrMetaDataContent.applyScripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = jsPath + script) {
        this.defer = true
    } }
}

fun HEAD.applyCoreScripts() {
    applyScripts(
        "utils.js",
        "tabs.js",
//        "logo.js",
        "koala/koala.js",
    )
}

val jsPath = "/www/js/"
val cssPath = "/www/css/"