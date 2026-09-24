package kampfire.api

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A user's public handle, made of letters, digits and `_`. */
@JvmInline
@Serializable
value class Username(override val value: String): SlugValue, LoginIdentity {
    override fun toString() = value

    companion object {
        val Empty = Username("")
        const val MAX_LENGTH = 24
        const val MIN_LENGTH = 3
    }
}

fun String.toUsername() = Username(this)

/** True when the length is within [Username.MIN_LENGTH] and [Username.MAX_LENGTH]. */
val Username.validUsernameLength get() = value.length in Username.MIN_LENGTH..Username.MAX_LENGTH
/** The first character that is not a letter, a digit or `_`, or `null` when there is none. */
fun Username.getInvalidCharacter() = value.firstOrNull { !it.isLetterOrDigit() && it != '_' }
val Username.validUsernameChars get() = getInvalidCharacter() == null
val Username.isValid get() = validUsernameLength && validUsernameChars

/**
 * Returns the username as [Ok] when its characters and length are valid, and a [Problem] naming the fault otherwise.
 */
fun Username.toValidOutcome(): Outcome<Username> = when {
    !validUsernameChars -> Problem("Username must be letters, numbers, or _")
    !validUsernameLength -> Problem("Username must be within ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters")
    else -> Ok(this)
}