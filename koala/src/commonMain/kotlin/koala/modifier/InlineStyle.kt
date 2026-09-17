@file:Suppress("FunctionName")

package koala.modifier

data class InlineStyle<T: Any>(val property: Property<T>, val value: T): Modifier {
    override val unmodifier get() = property

    val stringValue get() = property.valueToString?.invoke(value) ?: styleValueOf(value)

    override fun toString() = "${property.identifier}: $stringValue"
}

object UnitProperty {
    val MinHeight = Property<String>("min-height", false)
    val Height = Property<String>("height", false)
    val Padding = Property<String>("padding", false)
    val Margin = Property<String>("margin", false)
    val Gap = Property<String>("gap", false)
    val MaxWidth = Property<String>("max-width", false)
    val BorderRadius = Property<String>("border-radius", false)
}

fun MinHeight(value: Int) = UnitProperty.MinHeight.to("calc(var(--unit) * $value)")
fun Height(value: Int) = UnitProperty.Height.to("calc(var(--unit) * $value)")
fun Padding(value: Int) = UnitProperty.Padding.to("calc(var(--unit) * $value)")
fun Gap(value: Int) = UnitProperty.Gap.to("calc(var(--unit) * $value)")
fun MaxWidth(value: Int) = UnitProperty.MaxWidth.to("calc(var(--unit) * $value)")
fun Margin(value: Int) = UnitProperty.Margin.to("calc(var(--unit) * $value)")
fun BorderRadius(value: Int) = UnitProperty.BorderRadius.to("calc(var(--unit) * $value)")