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
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellContentOf

fun FlowContent.headerOf(
    location: Location,
    editRoute: AppRoute?,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    // headerImage(location.name, location.images.medium, modifiers, block)
    featureHeader(
        title = location.label,
        descriptor = "at",
        subtitle = location.addressLine,
        image = location.image,
        description = location.description,
        mod = mod,
        cellContent = cellContentOf(location),
        editRoute = editRoute,
        links = location.links,
        block = block
    )
}

fun FlowContent.headerOf(
    galaxy: Galaxy,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    featureHeader(
        title = galaxy.name,
        descriptor = "a galaxy",
        subtitle = galaxy.tagline,
        image = galaxy.image,
        colorScheme = ColorScheme.Galaxy,
        description = galaxy.description,
        mod = mod,
        cellContent = cellContentOf(galaxy),
        links = emptyList(),
        // editRoute = GalaxyConfigRoute(galaxy.slug),
        block = block
    )
}

fun FlowContent.headerOf(
    star: Star,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(star.username.value, star.image?.medium, mod, block)
}