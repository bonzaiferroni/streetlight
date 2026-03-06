package streetlight.web.ui

import koala.html.headerOf
import kotlinx.html.FlowContent
import streetlight.model.data.Location

fun FlowContent.headerOf(location: Location) {
    headerOf(location.name, location.imageUrl)
}