package streetlight.web

object SvgPath {
    val bus = toPath("bus")
    val food = toPath("food")
    val guitar = toPath("guitar")
    val fellowship = toPath("social")
    val train = toPath("train")
    val transitStop = toPath("transit-stop")
}

private fun toPath(filename: String) = "../../svg/$filename.svg"