package koala

object JsFiles : FileSet<JsFile>() {
    val utils = add("utils.js")
    val tabs = add("tabs.js")
    val koala = add("koala/koala.js")
}

object CssFiles : FileSet<CssFile>() {
    val reset = add("reset.css")
    val styles = add("styles.css")
    val typography = add("typography.css")
    val button = add("button.css")
    val layout = add("layout.css")
    val animation = add("animation.css")
    val tabs = add("tabs.css")
    val logo = add("logo.css")
    val geoMap = add("geoMap.css")
    val sandbox = add("sandbox.css")
    val elements = add("elements.css") // isGenerated = true
    val utilities = add("utilities.css")
}

