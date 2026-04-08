package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
data class Url(val value: String) {
    override fun toString() = value
    val isAbsolute get() = value.startsWith("http")
    val isRelative get() = !isAbsolute
}