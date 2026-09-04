package streetlight.web.ui

import kampfire.model.large
import kampfire.model.medium
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Media
import streetlight.model.data.Star
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.cellContentOf
import streetlight.web.layouts.postedAtCell
import streetlight.web.layouts.starCell
import streetlight.web.shells.createPostMenu
import streetlight.web.shells.galaxyMenu

fun FlowContent.headerOf(
    location: Location,
    editRoute: AppRoute?,
    mod: ModifierSet? = null,
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
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    column {
        featureHeader(
            title = galaxy.name,
            descriptor = "a galaxy",
            subtitle = galaxy.tagline,
            image = galaxy.image,
            colorScheme = ColorScheme.Galaxy,
            description = galaxy.description,
            mod = mod,
            cellContent = cellContentOf(galaxy),
            links = emptyList(),
            // editRoute = GalaxyConfigRoute(galaxy.slug),
            block = block
        )
        row(modify(JustifyContentSpaceBetween)) {
            galaxyMenu(emptyList(), galaxy)
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
            heading2(it, modify(TextAlignCenter, AntiShadow, LineHeight115, MarginTop4))
        }

        media.image?.let {
            column(modify(SideBorder, BorderRadius1)) {
                image(it, modify(AlignSelfCenter, MaxHeight96, BorderRadius2, MoonShadow))
            }
        }

        card(modify(OverflowClip, Gap0, ZenBg, Padding0)) {
            cellBlock {
                starCell(media.username)
                postedAtCell(media.createdAt)
            }

            media.text?.let {
                column(modify(Padding2, AlignSelfCenter, MaxWidthTextBody, TextLarge)) {
                    markdown(it)
                }
            }
        }
    }
}