package koala.dom

import koala.css.InlineStyle
import koala.css.Property
import koala.css.Modifier
import koala.html.Queryable
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.css.CSSStyleDeclaration

fun <T: Element> T.unmodify(vararg modifier: Modifier): T {
    modifier.forEach { classList.remove(it.identifier) }
    return this
}
fun <T: Element> T.modify(vararg modifier: Modifier): T {
    modifier.forEach { classList.add(it.identifier) }
    return this
}
fun <T: Element> T.unmodify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.remove(it.identifier) }
    return this
}
fun <T: Element> T.modify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.add(it.identifier) }
    return this
}

fun <T: Element> T.trigger(modifier: Modifier): T {
    unmodify(modifier)
    window.requestAnimationFrame {
        modify(modifier)
    }
    return this
}

fun <T: Element> T.modifyAfterFrame(vararg modifier: Modifier): T {
    window.requestAnimationFrame {
        modify(*modifier)
    }
    return this
}

fun <T: Element> T.unmodifyAfterFrame(vararg modifier: Modifier): T {
    window.requestAnimationFrame {
        unmodify(*modifier)
    }
    return this
}

fun Element.isModified(modifier: Modifier) = classList.contains(modifier.identifier)

fun Element.toggle(modifier: Modifier) = classList.toggle(modifier.identifier)

fun <T> CSSStyleDeclaration.setProperty(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.value.toString())

fun <T> HTMLElement.setProperty(style: InlineStyle<T>) {
    this.style.setProperty(style.property.expression, style.value.toString())
}

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList().map {
    it as HTMLElement
}

fun querySelector(queryable: Queryable) = document.body!!.querySelector(queryable)
fun querySelectorAll(queryable: Queryable) = document.body!!.querySelectorAll(queryable)

fun CSSStyleDeclaration.removeProperty(property: Property<*>) = removeProperty(property.identifier)

private var Element.job: Job? get() = asDynamic().job
    set(value) {
        asDynamic().let {
            val existingJob = job
            if (existingJob != null && existingJob.isActive) {
                error("Active coroutine job cannot be replaced")
            }
            job = value
        }
    }

private var Element.scope: CoroutineScope? get() = asDynamic().scope
    set(value) {
        asDynamic().let {
            val existingScope = scope
            if (existingScope != null && existingScope.isActive) {
                error("Active coroutine scope cannot be replaced")
            }
            scope = value
        }
    }

fun Element.getElementScope(parentScope: CoroutineScope, cancelExistingScope: Boolean): CoroutineScope {
    if (cancelExistingScope) {
        this.job?.cancel()
    }
    val scope = scope
    if (scope != null && scope.isActive) {
        return scope
    }

    val job = SupervisorJob()
    this.job = job
    val newScope = CoroutineScope(parentScope.coroutineContext + job)
    this.scope = newScope
    return newScope
}