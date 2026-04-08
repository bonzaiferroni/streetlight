package koala.html

import koala.Css
import koala.Js
import koala.JsFile
import koala.SiteFile
import kotlinx.html.*

fun HTML.appHead(
    title: String,
    styles: String,
    block: HEAD.() -> Unit,
) {
    head {
        title { +title }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1" }
        // font

        applyFira()

        link { href = "/www/icon/foxicon.ico"; rel = "icon"}
        block()
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js") {
            defer = true
        }
        applyFiles(JsFile)
        style {
            unsafe {
                +styles
            }
        }
    }
}

fun HEAD.applyFiles(files: Collection<SiteFile>) {
    files.forEach { applyFile(it) }
}

fun HEAD.applyFile(file: SiteFile) {
    when (file) {
        is Js -> applyJsFile(file)
        is Css -> applyCssFile(file)
        else -> error("unsupported file: $file")
    }
}

fun FlowOrMetaDataOrPhrasingContent.applyJsFile(file: Js) {
    script(src = file.url.value) {
        this.defer = file.isDeferred
    }
}

fun HEAD.applyCssFile(file: Css) {
    link { rel = "stylesheet"; href = file.url.value }
}

fun HEAD.applyFira() {
    link(rel = "preconnect", href = "https://fonts.googleapis.com")
    link {
        rel = "preconnect"
        href = "https://fonts.gstatic.com"
        attributes["crossorigin"] = ""
    }
    link(
        rel = "stylesheet",
        href = "https://fonts.googleapis.com/css2?family=Fira+Sans:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap"
    )
}