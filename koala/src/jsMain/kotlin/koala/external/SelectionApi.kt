package koala.external

import org.w3c.dom.Range
import org.w3c.dom.Window

external interface SelectionApi {
    val rangeCount: Int
    fun getRangeAt(index: Int): Range
    fun removeAllRanges()
    fun addRange(range: Range)
}

fun Window.selection(): SelectionApi? =
    asDynamic().getSelection().unsafeCast<SelectionApi?>()