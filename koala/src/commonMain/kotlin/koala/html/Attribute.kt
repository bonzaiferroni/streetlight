package koala.html

import kampfire.api.Username
import kampfire.api.toSlug
import kampfire.api.toUsername
import kampfire.model.GeoPoint
import koala.Lottie
import koala.utils.jsonConfig
import kotlinx.html.CoreAttributeGroupFacade
import kotlin.uuid.Uuid

data class Attribute<T>(
    val name: String,
    val isCustom: Boolean = false,
    val toStringValue: (T) -> String = { it.toString() },
    val toValue: ((String) -> T)? = null
): Queryable {
    override val selector get() = "[$identifier]"
    val identifier get() = when(isCustom) {
        true -> "data-$name"
        false -> name
    }

    fun to(value: T) = AttributeValue(this, value)
    fun selector(value: T) = AttributeValue(this, value).selector

    override fun toString() = selector

    companion object {
        val BlockLabel = stringAttributeOf("block-label", true)
        val Lottie = Attribute<Lottie>("lottie", true)
        val IsOn = booleanAttributeOf("is-on", true)
        val GeoPointAttribute = Attribute("geo-point", true) { GeoPoint.fromString(it) }
        val TabIndex = intAttributeOf("tab-index", true)
        val RoutePath = stringAttributeOf("route-path", true)
        val Placeholder = stringAttributeOf("placeholder", true)
        val Username = Attribute<Username?>("username", true) { it.toUsername() }

        val PopoverTarget = stringAttributeOf("popovertarget")
        val PopoverTargetAction = stringAttributeOf("popovertargetaction")
        val Popover = stringAttributeOf("popover")
        val SrcSet = stringAttributeOf("srcset")
        val Sizes = stringAttributeOf("sizes")
        val AriaLabel = stringAttributeOf("aria-label")
        val AriaMultiline = booleanAttributeOf("aria-multiline")
        val ContentEditable = stringAttributeOf("contenteditable")
        val Role = stringAttributeOf("role")
        val Spellcheck = booleanAttributeOf("spellcheck")
        val Draggable = booleanAttributeOf("draggable")
        val Autofocus = booleanAttributeOf("autofocus")
    }
}

fun <T> uuidAttributeOf(identifier: String, block: (Uuid) -> T ) =
    Attribute(identifier, true) { block(Uuid.parse(it)) }

fun stringAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it }

fun booleanAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it.toBoolean() }

fun intAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it.toInt() }

fun slugAttributeOf(identifier: String) = Attribute(identifier, false) { it.toSlug() }

inline fun <reified T: Enum<T>> enumAttributeOf(identifier: String) =
    Attribute<T>(identifier, true, { it.name }) { enumValueOf(it) }

inline fun <reified T> jsonAttributeOf(identifier: String) =
    Attribute<T>(identifier, true, jsonConfig::encodeToString, jsonConfig::decodeFromString)

data class AttributeValue<T>(val attribute: Attribute<T>, val value: T): Queryable {
    override val selector get() = "[${attribute.identifier}='${attribute.toStringValue(value)}']"

    override fun toString() = selector
}

fun CoreAttributeGroupFacade.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

fun <T> CoreAttributeGroupFacade.setAttribute(expression: AttributeValue<T>) =
    setAttribute(expression.attribute, expression.value)

fun <T> CoreAttributeGroupFacade.setAttribute(attribute: Attribute<T>, value: T?) {
    if (value != null) {
        attributes[attribute.identifier] = value.toString()
    } else {
        attributes.remove(attribute.identifier)
    }
}

fun CoreAttributeGroupFacade.setPopoverTarget(id: Id, action: String? = null) {
    setAttribute(Attribute.PopoverTarget, id.identifier)
    action?.let {
        setAttribute(Attribute.PopoverTargetAction, it)
    }
}

var CoreAttributeGroupFacade.blockLabel: String?
    get() = attributes[Attribute.BlockLabel.identifier]
    set(value) {
        setAttribute(Attribute.BlockLabel, value)
    }

fun CoreAttributeGroupFacade.setAriaLabel(label: String) {
    setAttribute(Attribute.AriaLabel.to(label))
}