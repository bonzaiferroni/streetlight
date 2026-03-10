package streetlight.web.ui

import koala.css.ModifierSet
import koala.dom.*
import streetlight.model.data.Location

fun RenderContext.cardOf(
    location: Location,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
) {
    cardOf(
        title = location.name,
        thumbUrl = location.thumbUrl,
        description = location.description,
        modifiers = modifiers,
        onClick = onClick,
    )
}