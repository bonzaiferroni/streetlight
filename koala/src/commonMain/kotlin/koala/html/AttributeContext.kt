package koala.html

import kampfire.api.TableId
import kotlinx.html.CoreAttributeGroupFacade
import kotlin.jvm.JvmInline

typealias AttributeContext = CoreAttributeGroupFacade

var AttributeContext.blockLabel: String?
    get() = attributes[Attribute.blockLabel.key]
    set(value) {
        attributes[Attribute.blockLabel.key] = value ?: ""
    }

@JvmInline
value class Attribute(val value: String): Queryable {
    override val selector get() = "[$key]"
    val key get() = "data-$value"

    companion object {
        val blockLabel = Attribute("block-label")
        val lottie = Attribute("lottie")
        val isOn = Attribute("is-on")
    }
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