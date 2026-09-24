package koala

import kampfire.model.Url
import kampfire.model.toUrl

/** A file the page loads, by URL. */
sealed interface Asset {
    val url: Url
    val assetType: AssetType
}

/** A set of assets, declared as properties that add themselves as they are created. */
@Suppress("UNCHECKED_CAST") // td: refactor
open class FileSet<T : Asset> : MutableSet<T> by mutableSetOf() {
    /** Adds the asset of [filename], its type and folder chosen by its extension. */
    fun add(filename: String, isGenerated: Boolean = false): T {
        val file = fileOf(filename, isGenerated) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }

    /** Adds the script [filename]. */
    fun addJs(filename: String, isDeferred: Boolean = true): T {
        val file = jsFileOf(filename, isDeferred) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }

    /** Adds the Lottie animation [filename]. */
    fun addLottie(filename: String): T {
        val file = Lottie((lottiePath + filename).toUrl()) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }
}

/** The kinds of [Asset]. */
enum class AssetType {
    Javascript,
    Stylesheet,
    Lottie,
    Svg,
    Image,
}

data class Stylesheet(override val url: Url): Asset {
    override val assetType get() = AssetType.Stylesheet
}

/** A script, deferred unless [isDeferred] is off. */
data class Js(override val url: Url, val isDeferred: Boolean = true): Asset {
    override val assetType get() = AssetType.Javascript
}

/** A Lottie animation. */
data class Lottie(override val url: Url): Asset {
    override val assetType get() = AssetType.Lottie
    override fun toString() = url.value
}

/** An SVG image, usually shown as an icon. */
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

/**
 * The asset of [filename], its type and folder chosen by its extension, or the generated folder when
 * [isGenerated]. Throws for an unsupported file.
 */
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
        AssetType.Stylesheet -> Stylesheet(url)
        AssetType.Svg -> Svg(url)
        AssetType.Image -> Image(url)
        else -> error("unsupported type: $type")
    }
}

/** The script [filename] under [basePath]. */
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