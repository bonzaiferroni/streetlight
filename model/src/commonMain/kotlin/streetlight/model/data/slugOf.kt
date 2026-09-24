package streetlight.model.data

import kampfire.api.Slug
import kampfire.api.toSlug

/** [name] as a slug: lowercase, hyphens for whitespace, other symbols removed. */
fun slugOf(name: String): Slug =
    name
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "-")
        .replace("[^a-z0-9\\-]".toRegex(), "").toSlug()