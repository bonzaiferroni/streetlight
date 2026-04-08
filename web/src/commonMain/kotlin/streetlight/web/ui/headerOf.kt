package streetlight.web.ui

import kampfire.model.large
import kampfire.model.medium
import koala.css.ModifierSet
import koala.html.headerImage
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Star

fun FlowContent.locationHeader(
    location: Location,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(location.name, location.images.medium, modifiers, block)
}

fun FlowContent.galaxyHeader(
    galaxy: Galaxy,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(galaxy.name, galaxy.images.large, modifiers, block)
}

fun FlowContent.starHeader(
    star: Star,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(star.name, star.images.medium, modifiers, block)
}