package kampfire.api

enum class ActionResult {
    Invalid,
    Problem,
    Success;

    companion object {
        fun of(arg: String?) = arg?.toIntOrNull()?.let { ActionResult.entries.getOrNull(it) } ?: Problem
    }
}