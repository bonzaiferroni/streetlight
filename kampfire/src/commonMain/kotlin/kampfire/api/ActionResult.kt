package kampfire.api

enum class ActionResult {
    Invalid,
    InternalError,
    Success;

    companion object {
        fun of(arg: String?) = arg?.toIntOrNull()?.let { ActionResult.entries.getOrNull(it) } ?: InternalError
    }
}