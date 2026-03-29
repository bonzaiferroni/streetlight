package koala.html

import kotlinx.html.Tag
import kotlinx.serialization.json.Json

inline fun <reified T> Tag.setJsonData(attribute: Attribute<T>, data: T) {
    attributes[attribute.key] = Json.encodeToString(data)
}

fun <T> Tag.setData(attribute: Attribute<T>, value: T) {
    attributes[attribute.key] = value.toString()
}