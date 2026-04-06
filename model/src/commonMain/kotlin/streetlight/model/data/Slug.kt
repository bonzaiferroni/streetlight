package streetlight.model.data

typealias Slug = String

fun slugOf(name: String): Slug =
    name
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "")