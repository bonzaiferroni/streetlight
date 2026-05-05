package streetlight.web.ui

import koala.dom.ViewContext
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
val AppContext.toaster get() = model.toaster

suspend inline fun <reified T> readIslandOrApi(
    id: Id,
    block: suspend () -> T
): T? = readIsland(id) ?: block()
