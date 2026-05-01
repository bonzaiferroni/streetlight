package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.queryJsonAttribute
import koala.dom.readIsland
import koala.html.Attribute
import koala.html.Id
import kotlinx.browser.document
import org.w3c.dom.Document
import streetlight.model.data.Galaxy
import streetlight.web.model.Streetlight

typealias AppContext = ViewContext<Streetlight>

val AppContext.api get() = model.client.api
val AppContext.portal get() = model.portal
val AppContext.userCache get() = model.cache

inline fun <reified T> AppContext.readInlineData(attribute: Attribute<T>): T? =
    document.body!!.queryJsonAttribute(attribute)?.value

suspend inline fun <reified T> AppContext.readInlineOrApi(
    attribute: Attribute<T>,
    block: suspend () -> T
): T? = readInlineData(attribute) ?: block()

suspend inline fun <reified T> AppContext.readIslandOrApi(
    id: Id,
    block: suspend () -> T
): T? = readIsland(id) ?: block()
