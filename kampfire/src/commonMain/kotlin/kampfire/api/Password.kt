package kampfire.api

import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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

@JvmInline
@Serializable
value class HashedPassword(val value: String)

val Password.validPasswordLength get() = value.length in Password.LENGTH_MIN..Password.LENGTH_MAX
val Password.strongPasswordLength get() = value.length >= Password.LENGTH_STRONG
val Password.bestPasswordLength get() = value.length >= Password.LENGTH_BEST

val Password.passwordHasDigit get() = value.any { it.isDigit() }
val Password.passwordHasSpecial get() = value.any { !it.isLetterOrDigit() }
val Password.passwordHasUppercase get() = value.any { it.isUpperCase() }
val Password.passwordHasLowercase get() = value.any { it.isLowerCase() }

val Password.aboveMinScore get() = lengthScore >= Password.SCORE_MIN

val Password.lengthScore: Int get() {
    var score = 0
    if (validPasswordLength) score++
    if (strongPasswordLength) score++
    if (bestPasswordLength) score++
    return score
}

val Password.complexityScore: Int get() {
    var score = 0
    if (passwordHasDigit) score++
    if (passwordHasSpecial) score++
    if (passwordHasUppercase) score++
    if (passwordHasLowercase) score++
    return score
}

val Password.totalScore: Int get() = lengthScore + complexityScore

val Password.passwordStrength get() = when (totalScore) {
    in 0..3 -> PasswordStrength.Invalid
    4 -> PasswordStrength.Weak
    5 -> PasswordStrength.Medium
    6 -> PasswordStrength.Strong
    7 -> PasswordStrength.Diamond
    else -> PasswordStrength.Invalid
}

enum class PasswordStrength: Labeled {
    Invalid, Weak, Medium, Strong, Diamond;

    override val label = name
}

fun Password.toValidOutcome(): Outcome<Password> = when {
    !validPasswordLength -> Problem(invalidLengthMessage)
    complexityScore < 3 -> Problem(invalidComplexityMessage)
    else -> Ok(this)
}

private val invalidLengthMessage = "Password must be between ${Password.LENGTH_MIN} and ${Password.LENGTH_MAX} characters"
private val invalidComplexityMessage = "Password must have at least 3: uppercase, lowercase, number, symbol"