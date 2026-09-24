package koala.modifier

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toSlug
import kampfire.api.toUsername
import kampfire.model.GeoPoint
import koala.Lottie
import koala.html.Id
import koala.html.Queryable
import koala.utils.jsonConfig
import kotlinx.html.CoreAttributeGroupFacade
import kotlin.uuid.Uuid

/**
 * An HTML attribute, with the conversions between its value and its text. A custom attribute is written with a
 * `data-` prefix.
 */
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

    /** An [AttributeValue] setting this attribute to [value]. */
    fun to(value: T) = AttributeValue(this, value)
    /** The selector of elements whose attribute holds [value]. */
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
        val Slug = Attribute<Slug?>("slug", true) { it.toSlug() }

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
        val ContentVisibility = stringAttributeOf("content-visibility")
        val ContainIntrinsicSize = stringAttributeOf("contain-intrinsic-size")
        val Disabled = unitAttributeOf("disabled")
    }
}

/** A custom attribute holding a record id, read with [block] from the [Uuid]. */
fun <T> idAttributeOf(identifier: String, block: (Uuid) -> T ) =
    Attribute(identifier, true) { block(Uuid.parse(it)) }

fun uuidAttributeOf(identifier: String) = Attribute(identifier, true) { Uuid.parse(it) }

fun stringAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it }

fun booleanAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it.toBoolean() }

fun intAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom) { it.toInt() }

fun slugAttributeOf(identifier: String) = Attribute(identifier, false) { it.toSlug() }

/** An attribute that is either present or absent, with no value. */
fun unitAttributeOf(identifier: String, isCustom: Boolean = false) =
    Attribute(identifier, isCustom, { "" }) { }

/** A custom attribute holding an enum entry by name. */
inline fun <reified T: Enum<T>> enumAttributeOf(identifier: String) =
    Attribute<T>(identifier, true, { it.name }) { enumValueOf(it) }

/** A custom attribute holding a value as JSON. */
inline fun <reified T> jsonAttributeOf(identifier: String) =
    Attribute<T>(identifier, true, jsonConfig::encodeToString, jsonConfig::decodeFromString)



/** Labels the element with [label] over its corner, or nothing when it is `null`. */
fun CoreAttributeGroupFacade.applyBlockLabel(label: String?) {
    label?.let {
        blockLabel = it
    }
}

/** Sets the attribute of [expression] to its value. */
fun <T> CoreAttributeGroupFacade.setAttribute(expression: AttributeValue<T>) {
    attributes[expression.attribute.identifier] = expression.toStringValue()
}

/** Sets [attribute] to [value], or removes it when [value] is `null`. */
fun <T> CoreAttributeGroupFacade.setAttribute(attribute: Attribute<T>, value: T?) {
    if (value != null) {
        attributes[attribute.identifier] = value.toString()
    } else {
        attributes.remove(attribute.identifier)
    }
}

/** Makes a click on this element act on the popover with [id]: toggle it, or [action] when given. */
fun CoreAttributeGroupFacade.setPopoverTarget(id: Id, action: String? = null) {
    setAttribute(Attribute.PopoverTarget, id.identifier)
    action?.let {
        setAttribute(Attribute.PopoverTargetAction, it)
    }
}

/** The label shown over the element's corner. */
var CoreAttributeGroupFacade.blockLabel: String?
    get() = attributes[Attribute.BlockLabel.identifier]
    set(value) {
        setAttribute(Attribute.BlockLabel, value)
    }

fun CoreAttributeGroupFacade.setAriaLabel(label: String) {
    setAttribute(Attribute.AriaLabel.to(label))
}