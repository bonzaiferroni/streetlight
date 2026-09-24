package streetlight.model.data

/** The kinds of space comments belong to. */
enum class SpaceType(paramValue: String? = null) {
    Galaxy,
    Post;

    val paramValue = paramValue ?: name.lowercase()

    companion object {
        fun from(paramValue: String) = entries.firstOrNull { it.paramValue == paramValue }
            ?: error("TalkSpace not found: $paramValue")
    }
}