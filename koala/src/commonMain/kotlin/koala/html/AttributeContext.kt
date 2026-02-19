package koala.html

import kampfire.api.TableId
import kotlinx.html.CoreAttributeGroupFacade
import kotlin.jvm.JvmInline

typealias AttributeContext = CoreAttributeGroupFacade

var AttributeContext.blockLabel: String?
    get() = attributes[Attributes.blockLabel.value]
    set(value) {
        attributes[Attributes.blockLabel.value] = value ?: ""
    }

@JvmInline
value class Attribute(val value: String) {
    val selector get() = "[$value]"
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
    this[attribute.value] = value
}

operator fun MutableMap<String, String>.set(attribute: Attribute, value: TableId<String>) {
    this[attribute.value] = value.value
}