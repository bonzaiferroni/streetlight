@file:Suppress("CAST_NEVER_SUCCEEDS")

package streetlight.web.ui

import koala.html.AppRoute
import koala.html.Id
import web.dom.Document
import web.dom.ElementId
import web.html.HTMLElement

fun Document.setTitle(title: String) {
    this.title = "$title | Streetlight"
}

fun Document.setTitle(route: AppRoute) = setTitle(route.title)
fun Document.requireElement(id: Id) = getElementById(ElementId(id.identifier)) ?: error("element with id not found: $id")