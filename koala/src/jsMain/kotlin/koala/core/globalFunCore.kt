package koala.core

import koala.css.KoalaFun
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollOptions

val globalFunCore = listOf(
    KoalaFun.ScrollToId to ::scrollToId,
)

fun scrollToId(id: String) = document.getElementById(id)?.scrollIntoView(ScrollOptions(ScrollBehavior.SMOOTH))
