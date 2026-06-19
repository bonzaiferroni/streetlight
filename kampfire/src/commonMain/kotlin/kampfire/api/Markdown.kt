package kampfire.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Markdown(val value: String) {
    override fun toString() = value
}

fun String.toMarkdown() = Markdown(this)