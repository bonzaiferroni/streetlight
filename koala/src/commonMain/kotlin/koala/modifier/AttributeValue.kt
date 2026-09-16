package koala.modifier

import koala.html.Queryable

data class AttributeValue<T>(val attribute: Attribute<T>, val value: T): Modifier, Queryable {
    override val selector get() = "[${attribute.identifier}='${attribute.toStringValue(value)}']"
    override val unmodifier get() = attribute

    override fun toString() = selector

    fun toStringValue() = attribute.toStringValue(value)
}
