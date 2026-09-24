package koala.utils

import kotlinx.serialization.json.Json

/** The app's JSON format, ignoring unknown keys and leaving out `null` values. */
val jsonConfig = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/** The app's JSON format, indented for reading. */
val jsonPrettyConfig = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

/** [obj] as indented JSON. */
inline fun <reified T> prettyPrint(obj: T) = jsonPrettyConfig.encodeToString(obj)