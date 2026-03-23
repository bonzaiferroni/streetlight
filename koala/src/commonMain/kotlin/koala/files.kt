package koala

object JsFiles: FileSet<JsFile>() {
    val utils = add("utils.js")
    val tabs = add("tabs.js")
    val koala = add("koala/koala.js")
}

object CssFiles: FileSet<CssFile>() {
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

object SvgFiles: FileSet<SvgFile>() {
    val bus = add("bus.svg")
    val food = add("food.svg")
    val guitar = add("guitar.svg")
    val meet = add("social.svg")
    val train = add("train.svg")
    val transitStop = add("transit-stop.svg")
    val focus = add("focus.svg")
    val chevronDown = add("chevron-down.svg")
    val emptyProfile = add("empty-profile.svg")
    val flame = add("flame.svg")
    val starOutline = add("star-outline.svg")
    val starFilled = add("star-filled.svg")
    val plus = add("plus.svg")
    val check = add("check.svg")
    val trash = add("trash.svg")
    val edit = add("edit.svg")
    val settings = add("settings.svg")
}

