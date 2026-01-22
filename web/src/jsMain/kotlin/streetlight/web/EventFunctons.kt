package streetlight.web

import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLElement

val Event.element get() = target as? HTMLElement