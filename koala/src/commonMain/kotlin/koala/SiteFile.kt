package koala

import kampfire.model.Url
import kampfire.model.toUrl

sealed interface SiteFile {
    val url: Url
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

    fun addLottie(filename: String) = Lottie((lottiePath + filename).toUrl())
}

enum class SiteFileType {
    Javascript,
    Stylesheet,
    Lottie,
    Svg,
    Image,
}

data class Css(override val url: Url): SiteFile {
    override val type get() = SiteFileType.Stylesheet
}

data class Js(override val url: Url, val isDeferred: Boolean = true): SiteFile {
    override val type get() = SiteFileType.Javascript
}

data class Lottie(override val url: Url): SiteFile {
    override val type get() = SiteFileType.Lottie
    override fun toString() = url.value
}

data class Svg(override val url: Url): SiteFile {
    override val type get() = SiteFileType.Svg
    override fun toString() = url.value
}

data class Image(override val url: Url): SiteFile {
    override val type get() = SiteFileType.Image
    override fun toString() = url.value
}

const val cssPath = "/www/css/"
const val jsPath = "/www/js/"
const val lottiePath = "/www/lottie/"
const val svgPath = "/www/svg/"
const val imgPath = "/www/img/"
const val genPath = "/gen/"

fun siteImageOf(path: String) = Image("$imgPath$path".toUrl())

fun fileOf(filename: String, isGenerated: Boolean): SiteFile {
    val type = if (filename.endsWith(".css")) SiteFileType.Stylesheet
    else if (filename.endsWith(".js")) SiteFileType.Javascript
    else if (filename.endsWith(".svg")) SiteFileType.Svg
    else if (filename.split('.').getOrNull(1)?.let { imageExtensions.contains(it) } ?: false) SiteFileType.Image
    else error("unsupported file: $filename")

    val path = when {
        isGenerated -> genPath
        type == SiteFileType.Javascript -> jsPath
        type == SiteFileType.Stylesheet -> cssPath
        type == SiteFileType.Svg -> svgPath
        type == SiteFileType.Image -> imgPath
        else -> error("unsupported path: $filename")
    }

    val url = "$path$filename".toUrl()

    return when (type) {
        SiteFileType.Javascript -> Js(url)
        SiteFileType.Stylesheet -> Css(url)
        SiteFileType.Svg -> Svg(url)
        SiteFileType.Image -> Image(url)
        else -> error("unsupported type: $type")
    }
}

fun jsFileOf(
    filename: String,
    isDeferred: Boolean = true,
    basePath: String = jsPath,
) = Js((basePath + filename).toUrl(), isDeferred)

val imageExtensions = setOf(
    "jpg",
    "png",
    "webp",
    "gif",
)