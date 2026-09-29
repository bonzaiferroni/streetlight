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

/**
 * This JSON with `<`, `>` and `&` written as unicode escapes, so it can be written into HTML as is. The text
 * still parses to the same value.
 */
fun String.sanitizeJson() = htmlSensitiveChars.replace(this) { match ->
    "\\u" + match.value[0].code.toString(16).padStart(4, '0')
}

private val htmlSensitiveChars = Regex("[<>&]")