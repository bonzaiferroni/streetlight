package koala

sealed interface SiteFile {
    val path: String
    val type: SiteFileType
}

open class FileSet<T : SiteFile> : MutableSet<T> by mutableSetOf() {
    @Suppress("UNCHECKED_CAST")
    fun add(filename: String, isGenerated: Boolean = false): T {
        val file = fileOf(filename, isGenerated) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }

    fun addJs(filename: String, isDeferred: Boolean = true) = jsFileOf(filename, isDeferred)

    fun addLottie(filename: String) = LottieFile(lottiePath + filename)
}

enum class SiteFileType {
    Javascript,
    Stylesheet,
    Lottie,
    Svg,
}

data class CssFile(override val path: String) : SiteFile {
    override val type get() = SiteFileType.Stylesheet
}

data class JsFile(override val path: String, val isDeferred: Boolean = true) : SiteFile {
    override val type get() = SiteFileType.Javascript
}

data class LottieFile(override val path: String) : SiteFile {
    override val type get() = SiteFileType.Lottie
}

data class SvgFile(override val path: String) : SiteFile {
    override val type get() = SiteFileType.Svg
}

const val cssPath = "/www/css/"
const val jsPath = "/www/js/"
const val lottiePath = "/www/lottie/"
const val svgPath = "/www/svg/"
const val genPath = "/gen/"

fun fileOf(filename: String, isGenerated: Boolean): SiteFile {
    val type = if (filename.endsWith(".css")) SiteFileType.Stylesheet
    else if (filename.endsWith(".js")) SiteFileType.Javascript
    else if (filename.endsWith(".svg")) SiteFileType.Svg
    else error("unsupported file: $filename")

    val path = when {
        isGenerated -> genPath
        type == SiteFileType.Javascript -> jsPath
        type == SiteFileType.Stylesheet -> cssPath
        type == SiteFileType.Svg -> svgPath
        else -> error("unsupported path: $filename")
    } + filename


    return when (type) {
        SiteFileType.Javascript -> JsFile(path)
        SiteFileType.Stylesheet -> CssFile(path)
        SiteFileType.Svg -> SvgFile(path)
        else -> error("unsupported type: $type")
    }
}

fun jsFileOf(filename: String, isDeferred: Boolean = true) = JsFile(jsPath + filename, isDeferred)
