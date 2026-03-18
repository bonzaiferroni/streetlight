package koala.html

import kampfire.api.TableId
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.impl.DelegatingMap
import kotlin.jvm.JvmInline

typealias AttributeContext = CoreAttributeGroupFacade

var AttributeContext.blockLabel: String?
    get() = attributes[Attributes.blockLabel.key]
    set(value) {
        attributes[Attributes.blockLabel.key] = value ?: ""
    }

@JvmInline
value class Attribute(val value: String) {
    val selector get() = "[$key]"
    val key get() = "data-$value"
}

object Attributes {
    val blockLabel = Attribute("block-label")
    val lottie = Attribute("lottie")
}

fun AttributeContext.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

operator fun MutableMap<String, String>.set(attribute: Attribute, value: String) {
    this[attribute.key] = value
}

operator fun MutableMap<String, String>.set(attribute: Attribute, value: TableId<String>) {
    this[attribute.key] = value.value
}