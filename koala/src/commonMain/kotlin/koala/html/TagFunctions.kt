package koala.html

import kotlinx.html.Tag
import kotlinx.serialization.json.Json

inline fun <reified T> Tag.setData(attribute: Attribute, data: T) {
    attributes[attribute.key] = Json.encodeToString(data)
}