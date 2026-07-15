package kampfire.api

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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

val Username.validUsernameLength get() = value.length in Username.MIN_LENGTH..Username.MAX_LENGTH
fun Username.getInvalidCharacter() = value.firstOrNull { !it.isLetterOrDigit() && it != '_' }
val Username.validUsernameChars get() = getInvalidCharacter() == null
val Username.isValid get() = validUsernameLength && validUsernameChars

fun Username.toValidOutcome(): Outcome<Username> = when {
    !validUsernameChars -> Problem("Username must be letters, numbers, or _")
    !validUsernameLength -> Problem("Username must be within ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters")
    else -> Ok(this)
}