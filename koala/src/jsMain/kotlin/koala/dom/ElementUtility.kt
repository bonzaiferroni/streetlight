package koala.dom

import js.array.asList
import kampfire.utils.takeEllipsis
import koala.modifier.Attribute
import koala.modifier.AttributeValue
import koala.modifier.ClassModifier
import koala.modifier.InlineStyle
import koala.modifier.Property
import koala.modifier.Modifier
import koala.modifier.ModifierSet
import koala.html.Queryable
import web.animations.requestAnimationFrame
import web.cssom.CSSStyleDeclaration
import web.cssom.ClassName
import web.dom.Document
import web.dom.Element
import web.dom.document
import web.html.HTMLElement

fun <T: Element> T.modify(modifier: Modifier): T {
    when (modifier) {
        is ClassModifier -> classList.add(modifier.className)
        is InlineStyle<*> -> style.setProperty(modifier.property.identifier, modifier.stringValue)
        is AttributeValue<*> -> setAttribute(modifier.attribute.identifier, modifier.toStringValue())
        is ModifierSet -> modifier.modifiers.forEach {
            it?.let { modify(it) }
        }
    }
    return this
}

fun <T: Element> T.unmodify(modifier: Modifier): T {
    when (val unmodifier = modifier.unmodifier) {
        is ClassModifier -> classList.remove(unmodifier.className)
        is Property<*> -> style.removeProperty(unmodifier.identifier)
        is Attribute<*> -> removeAttribute(unmodifier.identifier)
        null -> { }
    }
    return this
}

inline val Element.style: CSSStyleDeclaration
    get() = unsafeCast<HTMLElement>().style

inline val ClassModifier.className: ClassName
    get() = ClassName(identifier)

fun <T: Element> T.trigger(modifier: ClassModifier): T {
    classList.remove(modifier.className)
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

fun <T: HTMLElement> T.modifyAfterFrame(modifier: ClassModifier): T {
    requestAnimationFrame {
        classList.add(modifier.className)
    }
    return this
}

fun <T: HTMLElement> T.unmodifyAfterFrame(modifier: ClassModifier): T {
    requestAnimationFrame {
        classList.remove(modifier.className)
    }
    return this
}

fun HTMLElement.isModified(modifier: Modifier): Boolean = when (modifier) {
    is ClassModifier -> classList.contains(modifier.className)
    is InlineStyle<*> -> style.getPropertyValue(modifier.property.identifier) == modifier.stringValue
    is AttributeValue<*> -> getAttribute(modifier.attribute.identifier) == modifier.toStringValue()
    is ModifierSet -> modifier.modifiers.all { it == null || isModified(it) }
}

fun Element.toggle(modifier: ClassModifier) = classList.toggle(modifier.className)

// td: remove cast
fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Document.querySelector(queryable: Queryable) = querySelector(queryable.selector)
fun Document.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Element.closest(queryable: Queryable) = closest(queryable.selector)
fun Element.requireClosest(queryable: Queryable) = closest(queryable.selector) as? HTMLElement
    ?: error("closest not found: ${queryable.selector}")

fun Element.asHtmlElement() = this as HTMLElement

@Deprecated("query from document or element")
fun querySelector(queryable: Queryable) = document.body.querySelector(queryable)

fun CSSStyleDeclaration.removeStyle(property: Property<*>) = removeProperty(property.name)

fun Element.getPath(subject: Any? = null, limit: Int = Int.MAX_VALUE): String = buildString {
    subject?.let {
        append("[${it.toString().takeEllipsis(40)}]")
    }
    var count = 0
    var current: Element? = this@getPath
    while (current != null) {
        if (++count > limit) break
        if (isNotEmpty()) insert(0, " > ")
        val name = current.id.takeIf { it.toString().isNotEmpty() }?.let { "#$it" } ?: current.tagName.lowercase()
        insert(0, name)
        current = current.parentElement
    }
}

