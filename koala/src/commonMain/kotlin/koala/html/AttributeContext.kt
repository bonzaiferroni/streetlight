package koala.html

import kotlinx.html.CoreAttributeGroupFacade
import kotlin.jvm.JvmInline

typealias AttributeContext = CoreAttributeGroupFacade

var AttributeContext.blockLabel: String?
    get() = attributes[Attributes.blockLabel.value]
    set(value) {
        attributes[Attributes.blockLabel.value] = value ?: ""
    }

@JvmInline
value class Attribute(val value: String)

object Attributes {
    val blockLabel = Attribute("block-label")
}

fun AttributeContext.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}