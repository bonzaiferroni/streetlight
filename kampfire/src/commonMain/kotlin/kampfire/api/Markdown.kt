package kampfire.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Text written in markdown. */
@JvmInline
@Serializable
value class Markdown(val value: String) {

    companion object {
        val Empty = Markdown("")
    }

    override fun toString() = value

    val length get() = value.length

    fun isNotBlank() = value.isNotBlank()
}

fun String.toMarkdown() = Markdown(this)