package koala.html

import koala.CssFile
import koala.CssFiles
import koala.JsFile
import koala.JsFiles
import koala.SiteFile
import kotlinx.html.*

fun HTML.appHead(
    title: String,
    block: HEAD.() -> Unit,
) {
    head {
        title { +title }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1" }
        link { href = "/www/icon/foxicon.ico"; rel = "icon"}
        applyFiles(JsFiles)
        applyFiles(CssFiles)
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js") { }
        block()
    }
}

fun HEAD.applyFiles(files: Collection<SiteFile>) {
    files.forEach { applyFile(it) }
}

fun HEAD.applyFile(file: SiteFile) {
    when (file) {
        is JsFile -> applyJsFile(file)
        is CssFile -> applyCssFile(file)
        else -> error("unsupported file: $file")
    }
}

fun FlowOrMetaDataOrPhrasingContent.applyJsFile(file: JsFile) {
    script(src = file.path) {
        this.defer = file.isDeferred
    }
}

fun HEAD.applyCssFile(file: CssFile) {
    link { rel = "stylesheet"; href = file.path }
}