package streetlight.web.ui

import koala.html.AppRoute
import org.w3c.dom.Document

fun Document.setTitle(title: String) {
    this.title = "$title | Streetlight"
}

fun Document.setTitle(route: AppRoute) = setTitle(route.title)