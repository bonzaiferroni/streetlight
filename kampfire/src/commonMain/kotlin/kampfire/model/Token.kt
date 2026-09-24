package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A secret token sent to the user, such as in an email link. */
@Serializable
@JvmInline
value class Token(val value: String) {
    override fun toString() = value
}

/** The stored hash of a [Token]. */
@JvmInline
value class HashedToken(val value: String) {
    override fun toString() = value
}



