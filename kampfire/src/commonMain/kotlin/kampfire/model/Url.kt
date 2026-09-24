package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A URL, absolute, relative to the site, or a `blob:` URL of a local file. */
@JvmInline
@Serializable
value class Url(val value: String) {
    override fun toString() = value
    val isAbsolute get() = value.startsWith("http")
    val isRelative get() = value.startsWith("/")
    val isBlob get() = value.startsWith("blob")
    /** The last path segment when it has an extension, or `null`. */
    val filename get() = value.split('/').last().takeIf { it.contains('.') }
    /** The host of an absolute URL, or `null`. */
    val host get() = hostRegex.find(value)?.groupValues?.get(1)

    /** The path and query of the URL without its scheme, host and fragment. */
    fun toRelativePath(): String {
        val withoutScheme = value.substringAfter("://", value)
        val start = if (withoutScheme == value) 0 else withoutScheme.indexOf('/')
        val tail = if (start < 0) "/" else withoutScheme.substring(start)
        return tail.substringBefore('#').ifEmpty { "/" }
    }

    companion object {
        val Empty get() = Url("")
    }
}

fun String.toUrl() = Url(this)
/** True when the text is an `http` or `https` URL with a host. */
fun String.isValidAbsoluteUrl(): Boolean = absoluteUrlRegex.matches(this)
/** A [Url] of the text when [isValidAbsoluteUrl], and `null` otherwise. */
fun String.toValidAbsoluteUrlOrNull(): Url? = if (isValidAbsoluteUrl()) Url(this) else null

/** Inserts [appended] before the file extension, joined by a hyphen, as `photo-lg.jpg`. */
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

private val hostRegex = Regex("^https?://([^/?#]+)")
private val absoluteUrlRegex = Regex(
    "^https?://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(:\\d+)?(/[^\\s]*)?$",
    RegexOption.IGNORE_CASE
)