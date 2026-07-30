package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Token(val value: String) {
    override fun toString() = value
}

@JvmInline
value class HashedToken(val value: String) {
    override fun toString() = value
}



