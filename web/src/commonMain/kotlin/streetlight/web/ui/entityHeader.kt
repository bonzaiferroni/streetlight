package streetlight.web.ui

import koala.html.AppRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Entity
import streetlight.web.layouts.EntityCell
import streetlight.web.layouts.toCells
import streetlight.web.layouts.entityButtonsOf
import streetlight.web.layouts.toThemeColor

fun FlowContent.entityHeader(
    entity: Entity,
    descriptor: String,
    cells: List<EntityCell>? = entity.toCells(),
    editRoute: AppRoute? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) = pageHeader(
    title = entity.label,
    descriptor = descriptor,
    image = entity.image,
    subtitle = entity.sublabel,
    colorScheme = entity.toThemeColor(),
    description = entity.body,
    cells = cells,
    buttons = entityButtonsOf(entity, false),
    links = entity.links,
    editRoute = editRoute,
    mod = mod,
    block = block,
)
