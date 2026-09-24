package koala.interop

import koala.html.Id

/** A statement of JS to run inline. */
data class JsAction(val block: String)

/** Inline JS for common actions. */
object InlineJs {
    // language="JS"
    /** Hides the popover with [id]. */
    fun closePopover(id: Id) = JsAction("document.getElementById('${id.identifier}')?.hidePopover();")
}