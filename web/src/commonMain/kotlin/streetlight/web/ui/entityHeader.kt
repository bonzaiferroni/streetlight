package streetlight.web.ui

import koala.html.AppRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.FeedEntity
import streetlight.web.layouts.EntityCell
import streetlight.web.layouts.cells
import streetlight.web.layouts.entityButtonsOf
import streetlight.web.layouts.themeColor

fun FlowContent.entityHeader(
    entity: FeedEntity,
    descriptor: String,
    cells: List<EntityCell> = entity.cells,
    editRoute: AppRoute? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) = featureHeader(
    title = entity.label,
    descriptor = descriptor,
    image = entity.image,
    subtitle = entity.sublabel,
    colorScheme = entity.themeColor,
    description = entity.body,
    cells = cells,
    buttons = entityButtonsOf(entity, false),
    links = entity.links,
    editRoute = editRoute,
    mod = mod,
    block = block,
)
