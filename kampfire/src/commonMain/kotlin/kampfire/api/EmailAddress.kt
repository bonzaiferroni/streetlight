package kampfire.api

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** An email address, always lowercase. Construct it with [toEmailAddress], which normalizes the case. */
@JvmInline
@Serializable
value class EmailAddress(val value: String): LoginIdentity {
    override fun toString() = value
    init {
        require(value == value.lowercase()) { "Email must be normalized" }
    }
}

/** Masks the local part with [stars] bullets, keeping its first and last characters and the domain. */
fun EmailAddress.obfuscate(stars: Int = 10): String {
    val alias = value.substringBefore('@')
    val domain = value.substringAfter('@')
    val masked = "•".repeat(stars)
    return when (alias.length) {
        0 -> "@$domain"
        1 -> "${alias.first()}$masked@$domain"
        else -> "${alias.first()}$masked${alias.last()}@$domain"
    }
}

/**
 * Replaces the `@` with " at ", so the address can be shown where a scraper might harvest it.
 * [deobfuscateEmailForm] reverses it.
 */
fun EmailAddress.obfuscateForm() = value.replace("@", " at ")
/** Reads an address written by [obfuscateForm]. */
fun String.deobfuscateEmailForm() = EmailAddress(replace(" at ", "@"))

// this is the only valid constructor call
/**
 * Creates an [EmailAddress], normalizing it to lowercase. This is the only valid way to construct one.
 */
fun String.toEmailAddress() = EmailAddress(this.lowercase())

val EmailAddress.containsOneArroba get() = value.count { it == '@' } == 1
val EmailAddress.containsDot get() = value.split('@').getOrNull(1)?.contains('.') ?: false
/**
 * The first character that is neither a letter, a digit, nor an allowed symbol, or `null` when there is none.
 */
val EmailAddress.invalidCharacter get() = value.firstOrNull { !it.isLetterOrDigit() && !validEmailSymbols.contains(it) }
val EmailAddress.validCharacters get() = invalidCharacter == null

private val validEmailSymbols = setOf('@', '.', '+', '-', '_')

/** Checks the address's shape, returning it as [Ok] or a [Problem] naming the first fault. */
fun EmailAddress.toValidOutcome(): Outcome<EmailAddress> = when {
    !containsOneArroba -> Problem("Email is missing the @ symbol")
    !containsDot -> Problem("Email is missing a proper domain")
    !validCharacters -> Problem("Email has invalid character: $invalidCharacter")
    else -> Ok(this)
}

/** The identity a user signs in with: an [EmailAddress] or a [Username]. */
sealed interface LoginIdentity

/** Reads the text as an [EmailAddress] when it contains `@`, and as a [Username] otherwise. */
fun String.toLoginIdentity() = if (contains('@')) toEmailAddress() else toUsername()