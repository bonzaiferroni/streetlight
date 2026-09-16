package koala.html

import koala.modifier.Attribute
import koala.utils.jsonConfig
import kotlinx.html.Tag

inline fun <reified T> Tag.setJsonData(attribute: Attribute<T>, data: T) {
    attributes[attribute.identifier] = jsonConfig.encodeToString(data)
}

fun <T> Tag.setData(attribute: Attribute<T>, value: T) {
    attributes[attribute.identifier] = value.toString()
}