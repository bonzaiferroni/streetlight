package koala.dom

import koala.core.toggleAncestor
import koala.css.KoalaFun
import org.w3c.dom.HTMLElement

val globalFunExtended = listOf(
    KoalaFun.ToggleAncestor to ::toggleAncestor,
)
