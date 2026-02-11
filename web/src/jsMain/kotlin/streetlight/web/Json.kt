package streetlight.web

import kotlinx.serialization.json.Json


val jsonConfig = Json {
    ignoreUnknownKeys = true
}

val jsonPrettyConfig = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

inline fun <reified T> prettyJson(obj: T) = jsonPrettyConfig.encodeToString(obj)