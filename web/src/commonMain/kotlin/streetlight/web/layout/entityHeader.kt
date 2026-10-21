package streetlight.web.layout

import koala.Image
import koala.model.AppRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Entity
import streetlight.model.data.HeaderBlock
import streetlight.model.data.HeaderType
import streetlight.web.model.toCells
import streetlight.web.model.toButtons
import streetlight.web.model.toThemeColor

/**
 * The header of [entity] in the layout of [header], described by [descriptor], such as "a galaxy". The cells and
 * buttons, and the description and links, show only when [header] shows them.
 */
fun FlowContent.entityHeader(
    entity: Entity,
    descriptor: String,
    cells: List<EntityCell>? = entity.toCells(),
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
            colorScheme = entity.toThemeColor(),
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
            colorScheme = entity.toThemeColor(),
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
            colorScheme = entity.toThemeColor(),
            description = description,
            cells = shownCells,
            buttons = buttons,
            links = links,
            mod = mod,
            block = block,
        )
    }
}
