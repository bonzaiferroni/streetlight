@file:Suppress("FunctionName")

package koala.modifier

import kotlinx.css.Align
import kotlinx.css.Flex
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.px

data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    override val unmodifier get() = property

    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

operator fun Property<LinearDimension>.invoke(value: Int) = of(LinearDimension("calc(var(--unit) * $value)"))
operator fun Property<LinearDimension>.invoke(value: LinearDimension) = of(value)
