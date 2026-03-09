package streetlight.web.ui

import koala.html.headerOf
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location

fun FlowContent.headerOf(location: Location) {
    headerOf(location.name, location.imageUrl)
}

fun FlowContent.headerOf(galaxy: Galaxy) {
    headerOf(galaxy.name, galaxy.imageUrl)
}