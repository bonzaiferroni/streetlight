package koala.modifier

import koala.html.Queryable

/** An [attribute] set to [value], applied to an element and used as a selector. */
data class AttributeValue<T>(val attribute: Attribute<T>, val value: T): Modifier, Queryable {
    override val selector get() = "[${attribute.identifier}='${attribute.toStringValue(value)}']"

    override fun toString() = selector

    fun toStringValue() = attribute.toStringValue(value)
}
