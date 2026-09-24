package kampfire.model

/** The parts of an edit that are missing, as keys a form marks its fields with. */
data class ValidityCheck(
    val invalidParts: Set<String>
) {
    /** A message naming the missing parts, or `null` when none are missing. */
    val message get() = invalidParts.takeIf{ it.isNotEmpty() }?.let { "missing: ${it.joinToString(", ")}" }

    val isValid get() = invalidParts.isEmpty()
}

/** A [ValidityCheck] of these missing parts. */
fun Set<String>.toValidityCheck() = ValidityCheck(this)