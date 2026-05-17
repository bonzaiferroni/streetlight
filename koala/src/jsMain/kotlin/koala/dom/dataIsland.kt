package koala.dom

import koala.html.Id
import koala.utils.jsonConfig
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

inline fun <reified T> HTMLElement.readIsland(id: Id): T? {
    console.log("reading island")
    return querySelector(id)?.textContent?.let {
        jsonConfig.decodeFromString(it)
    }
}

inline fun <reified T> readIsland(id: Id): T? = document.body!!.readIsland<T>(id)