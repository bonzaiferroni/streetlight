package koala.html

import koala.modifier.Attribute
import koala.utils.jsonConfig
import kotlinx.html.Tag

/** Sets [attribute] to [data] as JSON. */
inline fun <reified T> Tag.setJsonData(attribute: Attribute<T>, data: T) {
    attributes[attribute.identifier] = jsonConfig.encodeToString(data)
}

/** Sets [attribute] to [value] as text. */
fun <T> Tag.setData(attribute: Attribute<T>, value: T) {
    attributes[attribute.identifier] = value.toString()
}