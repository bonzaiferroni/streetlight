package streetlight.web.layout

import koala.Image
import koala.model.AppRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Entity
import streetlight.web.model.toCells
import streetlight.web.model.toButtons
import streetlight.web.model.toThemeColor

/** The [pageHeader] of [entity], described by [descriptor], such as "a galaxy". */
fun FlowContent.entityHeader(
    entity: Entity,
    descriptor: String,
    cells: List<EntityCell>? = entity.toCells(),
    image: Image? = entity.image,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) = pageHeader(
    title = entity.label,
    descriptor = descriptor,
    image = image,
    subtitle = entity.sublabel,
    colorScheme = entity.toThemeColor(),
    description = entity.description,
    cells = cells,
    buttons = entity.toButtons(),
    links = entity.links,
    mod = mod,
    block = block,
)
