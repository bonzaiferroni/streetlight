package koala.html

import koala.LottieFile
import kotlinx.html.CoreAttributeGroupFacade
import kotlin.jvm.JvmInline

data class TagAttribute<T>(val identifier: String, val isCustom: Boolean = false): Queryable {
    override val selector get() = "[$key]"
    val key get() = when(isCustom) {
        true -> "data-$identifier"
        false -> identifier
    }

    companion object {
        val blockLabel = TagAttribute<String>("block-label", true)
        val lottie = TagAttribute<LottieFile>("lottie", true)
        val isOn = TagAttribute<Boolean>("is-on", true)
        // val maskUrl = TagAttribute<>("mask-url", true)
        val popoverTarget = TagAttribute<Id>("popovertarget")
    }
}

fun CoreAttributeGroupFacade.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

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
