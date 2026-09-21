package koala.modifier

import web.animations.requestAnimationFrame
import web.cssom.CSSStyleDeclaration
import web.cssom.ClassName
import web.dom.Element
import web.html.HTMLElement

fun <T: Element> T.modify(modifier: Modifier): T {
    when (modifier) {
        is Class -> classList.add(modifier.className)
        is InlineStyle<*> -> style.setProperty(modifier.property.identifier, modifier.stringValue)
        is AttributeValue<*> -> setAttribute(modifier.attribute.identifier, modifier.toStringValue())
        is ModifierSet -> modifier.modifiers.forEach {
            it?.let { modify(it) }
        }
    }
    return this
}

fun <T: Element> T.unmodify(modifier: Modifier): T {
    when (modifier) {
        is Class -> classList.remove(modifier.className)
        is InlineStyle<*> -> style.removeProperty(modifier.property.identifier)
        is AttributeValue<*> -> removeAttribute(modifier.attribute.identifier)
        is ModifierSet -> modifier.modifiers.forEach {
            it?.let { unmodify(it) }
        }
    }
    return this
}

inline val Element.style: CSSStyleDeclaration get() = unsafeCast<HTMLElement>().style
inline val Class.className: ClassName get() = ClassName(identifier)

fun <T: Element> T.trigger(modifier: Class): T {
    classList.remove(modifier.className)
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

fun <T: HTMLElement> T.modifyAfterFrame(modifier: Class): T {
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

fun <T: HTMLElement> T.unmodifyAfterFrame(modifier: Class): T {
    requestAnimationFrame {
        classList.remove(modifier.className)
    }
    return this
}

fun Element.isModified(modifier: Modifier): Boolean = when (modifier) {
    is Class -> classList.contains(modifier.className)
    is InlineStyle<*> -> style.getPropertyValue(modifier.property.identifier) == modifier.stringValue
    is AttributeValue<*> -> getAttribute(modifier.attribute.identifier) == modifier.toStringValue()
    is ModifierSet -> modifier.modifiers.all { it == null || isModified(it) }
}

fun Element.toggle(modifier: Modifier): Boolean {
    val isModified = !isModified(modifier)
    if (isModified) {
        modify(modifier)
    } else {
        unmodify(modifier)
    }
    return isModified
}

fun Modifier.contains(modifier: Modifier): Boolean {
    if (modifier is ModifierSet) return modifier.modifiers.all { it == null || contains(it) }
    return when (this) {
        is Class -> modifier is Class && identifier == modifier.identifier
        is InlineStyle<*> -> modifier is InlineStyle<*> &&
                property.identifier == modifier.property.identifier &&
                stringValue == modifier.stringValue
        is AttributeValue<*> -> modifier is AttributeValue<*> &&
                attribute.identifier == modifier.attribute.identifier &&
                toStringValue() == modifier.toStringValue()
        is ModifierSet -> modifiers.any { it != null && it.contains(modifier) }
    }
}
