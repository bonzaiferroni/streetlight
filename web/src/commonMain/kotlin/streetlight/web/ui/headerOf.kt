package streetlight.web.ui

import kampfire.model.large
import kampfire.model.medium
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Star
import streetlight.web.GalaxyConfigRoute
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.galaxyCells
import streetlight.web.layouts.locationCells

fun FlowContent.headerOf(
    location: Location,
    editRoute: AppRoute?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    // headerImage(location.name, location.images.medium, modifiers, block)
    featureHeader(
        title = location.label,
        descriptor = "at",
        subtitle = location.addressLine,
        image = location.images.large,
        description = location.description,
        modifiers = modifiers,
        cellContent = locationCells(location),
        editRoute = editRoute,
        links = location.links,
        block = block
    )
}

fun FlowContent.headerOf(
    galaxy: Galaxy,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    featureHeader(
        title = galaxy.name,
        descriptor = "a galaxy",
        subtitle = galaxy.tagline,
        image = galaxy.images.large,
        colorScheme = ColorScheme.Galaxy,
        description = galaxy.description,
        modifiers = modifiers,
        cellContent = galaxyCells(galaxy),
        links = emptyList(),
        editRoute = GalaxyConfigRoute(galaxy.slug),
        block = block
    )
}

fun FlowContent.headerOf(
    star: Star,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(star.username.value, star.images.medium, modifiers, block)
}