package streetlight.web.ui

import kampfire.model.thumb
import koala.SiteImage
import koala.css.ModifierSet
import koala.dom.*
import streetlight.model.data.Location

fun RenderContext.cardOf(
    location: Location,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
) {
    cardOf(
        title = location.displayTitle,
        thumbUrl = location.images.thumb ?: SiteImage.placeholderTh.url,
        description = location.description ?: location.address,
        modifiers = modifiers,
        onClick = onClick,
    )
}