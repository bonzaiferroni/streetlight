@file:Suppress("FunctionName")

package koala.modifier

import kotlinx.css.LinearDimension

data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    override val unmodifier get() = property

    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

val MinHeight = Property<String>("min-height", false)
val Height = Property<String>("height", false)
val Padding = Property<String>("padding", false)
val Margin = Property<String>("margin", false)
val Gap = Property<String>("gap", false)
val MaxWidth = Property<String>("max-width", false)
val BorderRadius = Property<String>("border-radius", false)

operator fun Property<String>.invoke(value: Int) = to("calc(var(--unit) * $value)")
operator fun Property<String>.invoke(value: LinearDimension) = to(value.value)
