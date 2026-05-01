package koala.dom

import koala.css.KoalaFun
import org.w3c.dom.HTMLElement

val globalFunExtended = listOf(
    KoalaFun.CallMenu to ::callMenu
)

fun callMenu(element: HTMLElement, menu: String, data: String) {
    console.log(data)
}