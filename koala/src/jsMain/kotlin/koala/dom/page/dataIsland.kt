package koala.dom.page

import koala.utility.jsonConfig
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import koala.dom.elementId
import koala.dom.querySelector
import koala.page.Id

/** Reads the data island with [elementId] under this element, or `null` when there is none. */
inline fun <reified T> HTMLElement.readIsland(elementId: Id): T? {
    console.log("reading island")
    return querySelector(elementId)?.textContent?.let {
        jsonConfig.decodeFromString(it)
    }
}

/** Reads the data island with [elementId] in the document body, or `null` when there is none. */
inline fun <reified T> readIsland(elementId: Id): T? = document.body!!.readIsland<T>(elementId)