package koala.html

import koala.utils.jsonConfig
import kotlinx.html.Tag
import kotlinx.serialization.json.Json

inline fun <reified T> Tag.setJsonData(attribute: Attribute<T>, data: T) {
    attributes[attribute.identifier] = jsonConfig.encodeToString(data)
}

fun <T> Tag.setData(attribute: Attribute<T>, value: T) {
    attributes[attribute.identifier] = value.toString()
}