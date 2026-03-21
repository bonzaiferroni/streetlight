package streetlight.web.ui

object SvgPath {
    val bus = toPath("bus")
    val food = toPath("food")
    val guitar = toPath("guitar")
    val meet = toPath("social")
    val train = toPath("train")
    val transitStop = toPath("transit-stop")
    val focus = toPath("focus")
    val chevronDown = toPath("chevron-down")
    val emptyProfile = toPath("empty-profile")
    val flame = toPath("flame")
    val starOutline = toPath("star-outline")
    val starFilled = toPath("star-filled")
    val plus = toPath("plus")
    val check = toPath("check")
    val trash = toPath("trash")
    val edit = toPath("edit")
    val settings = toPath("settings")
}

private fun toPath(filename: String) = "/www/svg/$filename.svg"