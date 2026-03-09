package streetlight.model.data

typealias PathId = String

fun pathIdFromName(name: String): String =
    name
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "_")
        .replace("[^a-z0-9_\\-]".toRegex(), "")