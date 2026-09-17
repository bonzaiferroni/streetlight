@file:Suppress("FunctionName")

package koala.modifier

import kotlinx.css.Align
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension

data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    override val unmodifier get() = property

    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

val MinHeight = Property<LinearDimension>("min-height")
val Height = Property<LinearDimension>("height")
val Padding = Property<LinearDimension>("padding")
val Margin = Property<LinearDimension>("margin")
val Gap = Property<LinearDimension>("gap")
val MaxWidth = Property<LinearDimension>("max-width")
val BorderRadius = Property<LinearDimension>("border-radius")

object CssProperty {
    val JustifyContent = Property<JustifyContent>("justify-content")
    val AlignItems = Property<Align>("align-items")
}

val JustifyContentCenter = CssProperty.JustifyContent.of(JustifyContent.center)
val AlignItemsCenter = CssProperty.AlignItems.of(Align.center)

operator fun Property<LinearDimension>.invoke(value: Int) = of(LinearDimension("calc(var(--unit) * $value)"))
operator fun Property<LinearDimension>.invoke(value: LinearDimension) = of(value)
