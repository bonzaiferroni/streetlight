package koala.modifier

import web.animations.requestAnimationFrame
import web.cssom.CSSStyleDeclaration
import web.cssom.ClassName
import web.dom.Element
import web.html.HTMLElement

/** Applies [modifier] to this live element. */
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

/** Removes [modifier] from this live element. */
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

/** Removes [modifier] and adds it back on the next frame, restarting any animation it starts. */
fun <T: Element> T.trigger(modifier: Class): T {
    classList.remove(modifier.className)
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

/** Adds [modifier] on the next animation frame, so a transition sees it arrive. */
fun <T: HTMLElement> T.modifyAfterFrame(modifier: Class): T {
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

/** Removes [modifier] on the next animation frame, so a transition sees it leave. */
fun <T: HTMLElement> T.unmodifyAfterFrame(modifier: Class): T {
    requestAnimationFrame {
        classList.remove(modifier.className)
    }
    return this
}

/** True when the element carries [modifier], every member for a set. */
fun Element.isModified(modifier: Modifier): Boolean = when (modifier) {
    is Class -> classList.contains(modifier.className)
    is InlineStyle<*> -> style.getPropertyValue(modifier.property.identifier) == modifier.stringValue
    is AttributeValue<*> -> getAttribute(modifier.attribute.identifier) == modifier.toStringValue()
    is ModifierSet -> modifier.modifiers.all { it == null || isModified(it) }
}

/** Applies [modifier] when the element lacks it and removes it otherwise, returning whether it is now applied. */
fun Element.toggle(modifier: Modifier): Boolean {
    val isModified = !isModified(modifier)
    if (isModified) {
        modify(modifier)
    } else {
        unmodify(modifier)
    }
    return isModified
}

/** True when every part of [modifier] is found in this one. */
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
