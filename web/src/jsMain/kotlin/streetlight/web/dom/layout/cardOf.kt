package streetlight.web.dom.layout

import koala.SiteImage
import koala.modifier.*
import koala.dom.*
import streetlight.model.data.Location
import koala.dom.component.*

fun ViewScope.cardOf(
    location: Location,
    mod: Modifier? = null,
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