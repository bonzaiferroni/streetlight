package koala.html

import koala.Lottie
import kotlinx.html.CoreAttributeGroupFacade

data class TagAttribute<T>(val identifier: String, val isCustom: Boolean = false): Queryable {
    override val selector get() = "[$key]"
    val key get() = when(isCustom) {
        true -> "data-$identifier"
        false -> identifier
    }

    fun to(value: T) = AttributeExpression(this, value)

    companion object {
        val blockLabel = TagAttribute<String>("block-label", true)
        val lottie = TagAttribute<Lottie>("lottie", true)
        val isOn = TagAttribute<Boolean>("is-on", true)
        // val maskUrl = TagAttribute<>("mask-url", true)
        val popoverTarget = TagAttribute<Id>("popovertarget")
        val popover = TagAttribute<String>("popover")
    }
}

data class AttributeExpression<T>(val attribute: TagAttribute<T>, val value: T)

fun CoreAttributeGroupFacade.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

fun <T> CoreAttributeGroupFacade.setAttribute(expression: AttributeExpression<T>) =
    setAttribute(expression.attribute, expression.value)

fun <T> CoreAttributeGroupFacade.setAttribute(attribute: TagAttribute<T>, value: T?) {
    if (value != null) {
        attributes[attribute.key] = value.toString()
    } else {
        attributes.remove(attribute.key)
    }
}

var CoreAttributeGroupFacade.blockLabel: String?
    get() = attributes[TagAttribute.blockLabel.key]
    set(value) {
        setAttribute(TagAttribute.blockLabel, value)
    }
