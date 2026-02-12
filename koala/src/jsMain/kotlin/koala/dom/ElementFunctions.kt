package koala.dom

import koala.css.CssClass
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element

fun Element.unmodify(vararg modifier: CssClass) = modifier.forEach { removeClass(it.value) }
fun Element.modify(vararg modifier: CssClass) = modifier.forEach { addClass(it.value) }
