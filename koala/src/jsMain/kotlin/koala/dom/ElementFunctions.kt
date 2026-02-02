package koala.dom

import koala.css.CssClass
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element

fun Element.unmodify(modifier: CssClass) = removeClass(modifier.value)
fun Element.modify(modifier: CssClass) = addClass(modifier.value)