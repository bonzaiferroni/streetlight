package koala.dom

import koala.css.Modifier
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Element

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { removeClass(it.value) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { addClass(it.value) }
