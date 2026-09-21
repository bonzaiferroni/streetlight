package streetlight.web.ui

import koala.modifier.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Media
import streetlight.model.data.Star
import streetlight.web.layouts.ThemeColor
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.cellContentOf
import streetlight.web.layouts.postedAtCell
import streetlight.web.layouts.starCell
import streetlight.web.shells.createPostMenu
import streetlight.web.shells.galaxyMenu

fun FlowContent.headerOf(
    location: Location,
    editRoute: AppRoute?,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    // headerImage(location.name, location.images.medium, modifiers, block)
    featureHeader(
        title = location.label,
        descriptor = "at",
        subtitle = location.addressLine,
        image = location.image,
        description = location.description,
        mod = mod,
        cellContent = cellContentOf(location),
        editRoute = editRoute,
        links = location.links,
        block = block
    )
}

fun FlowContent.headerOf(
    galaxy: Galaxy,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    column {
        featureHeader(
            title = galaxy.name,
            descriptor = "a galaxy",
            subtitle = galaxy.tagline,
            image = galaxy.image,
            colorScheme = ThemeColor.Galaxy,
            description = galaxy.description,
            mod = mod,
            cellContent = cellContentOf(galaxy),
            links = emptyList(),
            // editRoute = GalaxyConfigRoute(galaxy.slug),
            block = block
        )
        row(JustifyContentSpaceBetween) {
            galaxyMenu()
            createPostMenu(galaxy)
        }
    }
}

fun FlowContent.headerOf(
    star: Star
) {
    featureHeader(
        title = star.username.value,
        descriptor = "a streetlighter",
        image = star.image,
        subtitle = star.tagline,
        description = star.description,
    )
}

fun FlowContent.headerOf(media: Media) {
    column {
        media.title?.let {
            heading2(it, modify(TextAlignCenter, AntiShadow, LineHeight115, MarginTop(4)))
        }

        media.image?.let {
            column(modify(SideBorder, BorderRadius1)) {
                image(it, modify(AlignSelfCenter, MaxHeight(96), BorderRadius2, MoonShadow))
            }
        }

        card(modify(OverflowClip, Gap0, ZenBg, Padding(0))) {
            cellBlock {
                starCell(media.username)
                postedAtCell(media.createdAt)
            }

            media.text?.let {
                column(modify(Padding(2), AlignSelfCenter, MaxWidthTextBody, TextLarge)) {
                    markdown(it)
                }
            }
        }
    }
}