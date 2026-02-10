package koala.dom

import koala.css.Clickable
import koala.css.CssClass
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element
import org.w3c.dom.events.Event

fun Element.unmodify(modifier: CssClass) = removeClass(modifier.value)
fun Element.modify(modifier: CssClass) = addClass(modifier.value)

fun Element.onClick(block: (Event) -> Unit) {
    addEventListener("click", block)
    modify(Clickable)
}