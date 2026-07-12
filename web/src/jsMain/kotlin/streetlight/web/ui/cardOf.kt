package streetlight.web.ui

import kampfire.model.thumb
import koala.SiteImage
import koala.css.ModifierSet
import koala.dom.*
import streetlight.model.data.Location

fun AppScope.cardOf(
    location: Location,
    mod: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
) {
    cardOf(
        title = location.label,
        thumbUrl = location.image?.thumb ?: SiteImage.placeholderTh,
        description = location.description?.value ?: location.address,
        mod = mod,
        onClick = onClick,
    )
}