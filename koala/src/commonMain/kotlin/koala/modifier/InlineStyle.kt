@file:Suppress("FunctionName")

package koala.modifier

import kotlinx.css.LinearDimension

data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    override val unmodifier get() = property

    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

val MinHeight = Property<String>("min-height")
val Height = Property<String>("height")
val Padding = Property<String>("padding")
val Margin = Property<String>("margin")
val Gap = Property<String>("gap")
val MaxWidth = Property<String>("max-width")
val BorderRadius = Property<String>("border-radius")

operator fun Property<String>.invoke(value: Int) = to("calc(var(--unit) * $value)")
operator fun Property<String>.invoke(value: LinearDimension) = to(value.value)
