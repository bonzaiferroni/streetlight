package kampfire.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

/** A lowercase, hyphenated identifier used in a URL. Construct it with [toSlug]. */
@JvmInline
@Serializable
value class Slug(override val value: String): SlugValue {
    /** True when the slug starts with [base]. */
    fun hasBase(base: String) = value.startsWith(base)

    override fun toString() = value

    companion object {
        val Empty = Slug("")
        const val MAX_LENGTH = 36
        const val MIN_LENGTH = 3
    }
}

/**
 * Creates a [Slug] from the text, lowercased. It does not otherwise normalize; see [normalizeSlugSource].
 */
fun String.toSlug() = Slug(this.lowercase())

/**
 * Turns free text into slug form: lowercase, hyphens for whitespace, other symbols removed, and cut to
 * [Slug.MAX_LENGTH].
 */
fun String.normalizeSlugSource(): String =
    take(Slug.MAX_LENGTH)
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "")
        .replace("-+".toRegex(), "-")
        .trim('-')

private val VALID_SLUG = "^[a-z0-9]+(-[a-z0-9]+)*$".toRegex()

/**
 * True when the text is within the slug length bounds and made of lowercase letters and digits joined by single
 * hyphens.
 */
fun String.isValidSlug() =
    length in Slug.MIN_LENGTH..Slug.MAX_LENGTH && VALID_SLUG.matches(this)

fun Slug.isValid(): Boolean = value.isValidSlug()

/** A value that can stand as a slug in a URL. */
interface SlugValue {
    val value: String
}