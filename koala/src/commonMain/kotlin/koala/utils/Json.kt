package koala.utils

import kotlinx.serialization.json.Json

val jsonConfig = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

val jsonPrettyConfig = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

inline fun <reified T> prettyPrint(obj: T) = jsonPrettyConfig.encodeToString(obj)