package streetlight.compose.io

/** The text of the app-data file [name], or `null` when there is none. */
expect suspend fun readAppFile(name: String): String?

/** Writes [text] to the app-data file [name], readable by the user alone where the platform allows. */
expect suspend fun writeAppFile(name: String, text: String)

/** The text of the app-data file [name] read at once, for a value wanted before the app goes on, or `null`. */
expect fun readAppFileNow(name: String): String?
