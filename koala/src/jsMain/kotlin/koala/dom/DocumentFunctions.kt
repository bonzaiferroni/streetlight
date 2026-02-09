package koala.dom

import koala.html.Id
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

fun Document.getElementById(id: Id) = getElementById(id.value) as HTMLElement