package streetlight.model.data

import kampfire.api.Slug

fun slugOf(name: String): Slug =
    name
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "")