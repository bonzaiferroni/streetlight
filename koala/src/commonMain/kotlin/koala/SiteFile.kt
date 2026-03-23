package koala

sealed interface SiteFile {
    val path: String
}

open class FileSet<T: SiteFile>: MutableSet<T> by mutableSetOf() {
    @Suppress("UNCHECKED_CAST")
    fun add(filename: String, isGenerated: Boolean = false): T {
        val file = fileOf(filename, isGenerated) as? T ?: error("invalid file: $filename")
        add(file)
        return file
    }

    fun addJs(filename: String, isDeferred: Boolean = true) = jsFileOf(filename, isDeferred)
}

enum class SiteFileType {
    Javascript,
    Stylesheet,
}

data class CssFile(override val path: String): SiteFile
data class JsFile(override val path: String, val isDeferred: Boolean = true): SiteFile

const val cssPath = "/www/css/"
const val jsPath = "/www/js/"
const val genPath = "/gen/"

fun fileOf(filename: String, isGenerated: Boolean): SiteFile {
    val type = if (filename.endsWith(".css")) SiteFileType.Stylesheet
    else if (filename.endsWith(".js")) SiteFileType.Javascript
    else error("unsupported file: $filename")

    val path = when {
        isGenerated -> genPath
        type == SiteFileType.Javascript -> jsPath
        type == SiteFileType.Stylesheet -> cssPath
        else -> error("unsupported path: $filename")
    } + filename


    return when(type) {
        SiteFileType.Javascript -> JsFile(path)
        SiteFileType.Stylesheet -> CssFile(path)
    }
}

fun jsFileOf(filename: String, isDeferred: Boolean = true) = JsFile(jsPath + filename, isDeferred)