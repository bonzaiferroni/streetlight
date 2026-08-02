package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Url(val value: String) {
    override fun toString() = value
    val isAbsolute get() = value.startsWith("http")
    val isRelative get() = value.startsWith("/")
    val isBlob get() = value.startsWith("blob")
    val filename get() = value.split('/').last().takeIf { it.contains('.') }
    val host get() = Regex("^https?://([^/?#]+)").find(value)?.groupValues?.get(1)

    companion object {
        val Empty get() = Url("")
    }
}

fun String.toUrl() = Url(this)

fun String.appendToFilename(appended: String) = buildString {
    val parts = this@appendToFilename.split('.')
    parts.forEachIndexed { index, part ->
        if (index < parts.size - 2) {
            append(part)
            append(".")
        } else if (index == parts.size - 2) {
            append(part)
            append("-")
            append(appended)
            append('.')
        } else {
            append(part)
        }
    }
}