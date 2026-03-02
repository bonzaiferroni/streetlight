package streetlight.web.io

object SvgPath {
    val bus = toPath("bus")
    val food = toPath("food")
    val guitar = toPath("guitar")
    val meet = toPath("social")
    val train = toPath("train")
    val transitStop = toPath("transit-stop")
}

private fun toPath(filename: String) = "/www/svg/$filename.svg"