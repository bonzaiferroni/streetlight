package kampfire.api

/**
 * The outcome of an action taken from a link outside the app, such as an email, reported on its landing page.
 */
enum class ActionResult {
    Invalid,
    Problem,
    Success;

    companion object {
        /**
         * Reads the result from its ordinal in [arg], falling back to [Problem] when it is missing or unknown.
         */
        fun of(arg: String?) = arg?.toIntOrNull()?.let { ActionResult.entries.getOrNull(it) } ?: Problem
    }
}