@file:Suppress("CAST_NEVER_SUCCEEDS")

package streetlight.web.utility

import koala.model.WebRoute
import koala.page.Id
import web.dom.Document
import web.dom.ElementId
import web.html.HTMLElement

/** Sets the tab title to [title], followed by the site name. */
fun Document.setTitle(title: String) {
    this.title = "$title | Streetlight"
}

fun Document.setTitle(route: WebRoute) = setTitle(route.title)
fun Document.requireElement(id: Id) = getElementById(ElementId(id.identifier)) ?: error("element with id not found: $id")