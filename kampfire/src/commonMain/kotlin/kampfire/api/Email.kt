package kampfire.api

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Email(val value: String): LoginIdentity {
    init {
        require(value == value.lowercase()) { "Email must be normalized" }
    }
}

fun Email.obfuscate(stars: Int = 10): String {
    val alias = value.substringBefore('@')
    val domain = value.substringAfter('@')
    val masked = "•".repeat(stars)
    return when (alias.length) {
        0 -> "@$domain"
        1 -> "${alias.first()}$masked@$domain"
        else -> "${alias.first()}$masked${alias.last()}@$domain"
    }
}

// this is the only valid constructor call
fun String.toEmail() = Email(this.lowercase())

val Email.containsOneArroba get() = value.count { it == '@' } == 1
val Email.containsDot get() = value.split('@').getOrNull(1)?.contains('.') ?: false
val Email.invalidCharacter get() = value.firstOrNull { !it.isLetterOrDigit() && !validEmailSymbols.contains(it) }
val Email.validCharacters get() = invalidCharacter == null

private val validEmailSymbols = setOf('@', '.', '+', '-', '_')

fun Email.toValidOutcome(): Outcome<Email> = when {
    !containsOneArroba -> Problem("Email is missing the @ symbol")
    !containsDot -> Problem("Email is missing a proper domain")
    !validCharacters -> Problem("Email has invalid character: $invalidCharacter")
    else -> Ok(this)
}

sealed interface LoginIdentity

fun String.toLoginIdentity() = if (contains('@')) toEmail() else toUsername()