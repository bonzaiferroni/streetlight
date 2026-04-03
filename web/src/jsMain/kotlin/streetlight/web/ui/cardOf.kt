package streetlight.web.ui

import koala.css.ModifierSet
import koala.dom.*
import koala.html.SiteImage
import streetlight.model.data.Location

fun RenderContext.cardOf(
    location: Location,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
) {
    cardOf(
        title = location.name,
        thumbUrl = location.thumbUrl ?: SiteImage.placeholderThumb,
        description = location.description ?: location.address,
        modifiers = modifiers,
        onClick = onClick,
    )
}