package kampfire.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Username(override val value: String): SlugValue {
    override fun toString() = value

    companion object {
        val Empty = Username("")
        const val MAX_LENGTH = 24
        const val MIN_LENGTH = 2
    }
}

fun String.toUsername() = Username(this)