package streetlight.web.ui

import koala.css.ModifierSet
import koala.html.headerOf
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location

fun FlowContent.headerOf(
    location: Location,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerOf(location.name, location.imageUrl, modifiers, block)
}

fun FlowContent.headerOf(
    galaxy: Galaxy,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerOf(galaxy.name, galaxy.imageUrl, modifiers, block)
}