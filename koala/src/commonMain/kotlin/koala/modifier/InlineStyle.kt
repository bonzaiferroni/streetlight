@file:Suppress("FunctionName")

package koala.modifier

import kotlinx.css.Align
import kotlinx.css.Flex
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.px

/** A [property] set to [value] in an element's inline style. */
data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

/** The property set to [value] multiples of `--unit`. */
operator fun Property<LinearDimension>.invoke(value: Int) = of(LinearDimension("calc(var(--unit) * $value)"))
/** The property set to [value]. */
operator fun Property<LinearDimension>.invoke(value: LinearDimension) = of(value)
