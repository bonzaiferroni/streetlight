package kampfire.api

import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** A password in plain text, with the length and score bounds it is checked against. */
@JvmInline
@Serializable
value class Password(val value: String) {
    companion object {
        val Empty = Password("")

        const val LENGTH_MIN = 8
        const val LENGTH_STRONG = 12
        const val LENGTH_BEST = 16
        const val LENGTH_MAX = 64
        const val SCORE_MIN = 4
    }
}

/** A stored hash of a [Password]. */
@JvmInline
@Serializable
value class PasswordHash(val value: String)

// avoids only transit in plain text
/**
 * Scrambles the password so it does not travel as plain text. It is not encryption; [deobfuscatePassword] reverses it.
 */
fun Password.obfuscatePassword() = value.map { it.code.xor('s'.code).toChar() }.joinToString("")
/** Reads a password written by [obfuscatePassword]. */
fun String.deobfuscatePassword() = Password(map { it.code.xor('s'.code).toChar() }.joinToString(""))

/** True when the length is within [Password.LENGTH_MIN] and [Password.LENGTH_MAX]. */
val Password.validPasswordLength get() = value.length in Password.LENGTH_MIN..Password.LENGTH_MAX
val Password.strongPasswordLength get() = value.length >= Password.LENGTH_STRONG
val Password.bestPasswordLength get() = value.length >= Password.LENGTH_BEST

val Password.passwordHasDigit get() = value.any { it.isDigit() }
val Password.passwordHasSpecial get() = value.any { !it.isLetterOrDigit() }
val Password.passwordHasUppercase get() = value.any { it.isUpperCase() }
val Password.passwordHasLowercase get() = value.any { it.isLowerCase() }

/** True when the [lengthScore] reaches [Password.SCORE_MIN]. */
val Password.aboveMinScore get() = lengthScore >= Password.SCORE_MIN

/** One point each for a valid, a strong, and a best length. */
val Password.lengthScore: Int get() {
    var score = 0
    if (validPasswordLength) score++
    if (strongPasswordLength) score++
    if (bestPasswordLength) score++
    return score
}

/** One point each for a digit, a symbol, an uppercase letter and a lowercase letter. */
val Password.complexityScore: Int get() {
    var score = 0
    if (passwordHasDigit) score++
    if (passwordHasSpecial) score++
    if (passwordHasUppercase) score++
    if (passwordHasLowercase) score++
    return score
}

/** The sum of [lengthScore] and [complexityScore]. */
val Password.totalScore: Int get() = lengthScore + complexityScore

/** The [PasswordStrength] of the [totalScore]. */
val Password.passwordStrength get() = when (totalScore) {
    in 0..3 -> PasswordStrength.Invalid
    4 -> PasswordStrength.Weak
    5 -> PasswordStrength.Medium
    6 -> PasswordStrength.Strong
    7 -> PasswordStrength.Diamond
    else -> PasswordStrength.Invalid
}

/** The strength of a password, from its [totalScore]. */
enum class PasswordStrength: Labeled {
    Invalid, Weak, Medium, Strong, Diamond;

    override val label = name
}

/**
 * Returns the password as [Ok] when its length is valid and it has at least three kinds of character, and a
 * [PasswordProblem] otherwise.
 */
fun Password.toValidOutcome(): Outcome<Password> = when {
    !validPasswordLength -> PasswordProblem.InvalidLength
    complexityScore < 3 -> PasswordProblem.InvalidComplexity
    else -> Ok(this)
}

/** The problems a password check reports. */
object PasswordProblem {
    val InvalidComplexity = Problem("Password must have at least 3: uppercase, lowercase, number, symbol")
    val InvalidLength = Problem("Password must be between ${Password.LENGTH_MIN} and ${Password.LENGTH_MAX} characters")
}