package streetlight.model.data

import kampfire.api.Slug
import kampfire.api.toSlug

fun slugOf(name: String): Slug =
    name
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "").toSlug()