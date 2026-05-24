package kampfire.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Slug(val string: String) {
    fun hasBase(base: String) = string.startsWith(base)

    override fun toString() = string

    companion object {
        val Empty = Slug("")
        const val MAX_LENGTH = 36
        const val MIN_LENGTH = 3
    }
}

fun String.toSlug() = Slug(this)

fun String.normalizeSlugSource(): String =
    take(Slug.MAX_LENGTH)
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "")
        .replace("-+".toRegex(), "-")
        .trim('-')

private val VALID_SLUG = "^[a-z0-9]+(-[a-z0-9]+)*$".toRegex()

fun Slug.isValid(): Boolean =
    string.length in Slug.MIN_LENGTH..Slug.MAX_LENGTH && VALID_SLUG.matches(string)