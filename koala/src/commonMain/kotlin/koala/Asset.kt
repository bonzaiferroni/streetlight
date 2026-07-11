package koala

import kampfire.model.Url
import kampfire.model.toUrl

sealed interface Asset {
    val url: Url
    val assetType: AssetType
}

open class FileSet<T : Asset> : MutableSet<T> by mutableSetOf() {
    @Suppress("UNCHECKED_CAST")
    fun add(filename: String, isGenerated: Boolean = false): T {
        val file = fileOf(filename, isGenerated) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }

    fun addJs(filename: String, isDeferred: Boolean = true) = jsFileOf(filename, isDeferred)

    fun addLottie(filename: String) = Lottie((lottiePath + filename).toUrl())
}

enum class AssetType {
    Javascript,
    Stylesheet,
    Lottie,
    Svg,
    Image,
}

data class Css(override val url: Url): Asset {
    override val assetType get() = AssetType.Stylesheet
}

data class Js(override val url: Url, val isDeferred: Boolean = true): Asset {
    override val assetType get() = AssetType.Javascript
}

data class Lottie(override val url: Url): Asset {
    override val assetType get() = AssetType.Lottie
    override fun toString() = url.value
}

data class Svg(override val url: Url): Asset {
    override val assetType get() = AssetType.Svg
    override fun toString() = url.value
}

const val cssPath = "/www/css/"
const val jsPath = "/www/js/"
const val lottiePath = "/www/lottie/"
const val svgPath = "/www/svg/"
const val imgPath = "/www/img/"
const val genPath = "/gen/"

fun fileOf(filename: String, isGenerated: Boolean): Asset {
    val type = if (filename.endsWith(".css")) AssetType.Stylesheet
    else if (filename.endsWith(".js")) AssetType.Javascript
    else if (filename.endsWith(".svg")) AssetType.Svg
    else if (filename.split('.').getOrNull(1)?.let { imageExtensions.contains(it) } ?: false) AssetType.Image
    else error("unsupported file: $filename")

    val path = when {
        isGenerated -> genPath
        type == AssetType.Javascript -> jsPath
        type == AssetType.Stylesheet -> cssPath
        type == AssetType.Svg -> svgPath
        type == AssetType.Image -> imgPath
        else -> error("unsupported path: $filename")
    }

    val url = "$path$filename".toUrl()

    return when (type) {
        AssetType.Javascript -> Js(url)
        AssetType.Stylesheet -> Css(url)
        AssetType.Svg -> Svg(url)
        AssetType.Image -> Image(url)
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