package koala.interop

import koala.html.Id

data class JsAction(val block: String)

object InlineJs {
    // language="JS"
    fun closePopover(id: Id) = JsAction("document.getElementById('${id.identifier}')?.hidePopover();")
}