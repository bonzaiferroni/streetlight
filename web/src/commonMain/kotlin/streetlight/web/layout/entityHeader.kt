package streetlight.web.layout

import koala.Image
import koala.model.WebRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Entity
import streetlight.model.data.HeaderBlock
import streetlight.model.data.HeaderType
import streetlight.model.data.toDetails
import streetlight.web.model.toButtons
import streetlight.model.data.toEntityType
import streetlight.model.data.EntityDetail

/**
 * The header of [entity] in the layout of [header], described by [descriptor], such as "a galaxy". The cells and
 * buttons, and the description and links, show only when [header] shows them.
 */
fun FlowContent.entityHeader(
    entity: Entity,
    descriptor: String,
    cells: List<EntityDetail>? = entity.toDetails(),
    image: Image? = entity.image,
    header: HeaderBlock = HeaderBlock(),
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    val shownCells = cells.takeIf { header.showCells }
    val buttons = entity.toButtons().takeIf { header.showCells }
    val description = entity.description.takeIf { header.showBody }
    val links = entity.links.takeIf { header.showBody }

    when (header.headerType) {
        HeaderType.Card -> pageHeader(
            title = entity.label,
            descriptor = descriptor,
            image = image,
            subtitle = entity.sublabel,
            colorScheme = entity.toEntityType(),
            description = description,
            cells = shownCells,
            buttons = buttons,
            links = links,
            mod = mod,
            block = block,
        )
        HeaderType.Hero -> heroHeader(
            title = entity.label,
            descriptor = descriptor,
            image = image,
            subtitle = entity.sublabel,
            colorScheme = entity.toEntityType(),
            description = description,
            cells = shownCells,
            buttons = buttons,
            links = links,
            mod = mod,
            block = block,
        )
        HeaderType.Marquee -> marqueeHeader(
            title = entity.label,
            descriptor = descriptor,
            image = image,
            subtitle = entity.sublabel,
            colorScheme = entity.toEntityType(),
            description = description,
            cells = shownCells,
            buttons = buttons,
            links = links,
            mod = mod,
            block = block,
        )
    }
}
