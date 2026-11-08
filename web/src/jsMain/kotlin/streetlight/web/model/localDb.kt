package streetlight.web.model

import streetlight.web.utility.IdbDatabase

/** The browser database holding what a user keeps on the device. A new store joins the list and raises the version. */
fun localDbOf() = IdbDatabase(LOCAL_DB_NAME, version = 1, storeNames = listOf(CLIP_STORE, MIX_STORE))

internal const val CLIP_STORE = "clips"
internal const val MIX_STORE = "mixes"

private const val LOCAL_DB_NAME = "streetlight"
