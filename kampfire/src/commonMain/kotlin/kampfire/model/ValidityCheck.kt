package kampfire.model

data class ValidityCheck(
    val invalidParts: Set<String>
) {
    val message get() = invalidParts.takeIf{ it.isNotEmpty() }?.let { "missing: ${it.joinToString(", ")}" }

    val isValid get() = invalidParts.isEmpty()
}

fun Set<String>.toValidityCheck() = ValidityCheck(this)