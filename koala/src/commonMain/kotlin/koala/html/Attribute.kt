package koala.html

import koala.Lottie
import kotlinx.html.CoreAttributeGroupFacade

data class Attribute<T>(val identifier: String, val isCustom: Boolean = false): Queryable {
    override val selector get() = "[$key]"
    val key get() = when(isCustom) {
        true -> "data-$identifier"
        false -> identifier
    }

    fun to(value: T) = AttributeExpression(this, value)

    override fun toString() = selector

    companion object {
        val BlockLabel = Attribute<String>("block-label", true)
        val Lottie = Attribute<Lottie>("lottie", true)
        val IsOn = Attribute<Boolean>("is-on", true)

        val PopoverTarget = Attribute<String>("popovertarget")
        val Popover = Attribute<String>("popover")
    }
}

data class AttributeExpression<T>(val attribute: Attribute<T>, val value: T)

fun CoreAttributeGroupFacade.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

fun <T> CoreAttributeGroupFacade.setAttribute(expression: AttributeExpression<T>) =
    setAttribute(expression.attribute, expression.value)

fun <T> CoreAttributeGroupFacade.setAttribute(attribute: Attribute<T>, value: T?) {
    if (value != null) {
        attributes[attribute.key] = value.toString()
    } else {
        attributes.remove(attribute.key)
    }
}

var CoreAttributeGroupFacade.blockLabel: String?
    get() = attributes[Attribute.BlockLabel.key]
    set(value) {
        setAttribute(Attribute.BlockLabel, value)
    }
