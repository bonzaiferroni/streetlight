package streetlight.web.ui

import koala.modifier.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Media
import streetlight.model.data.Star
import streetlight.web.layouts.cellGrid
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
        entity = location,
        descriptor = "at",
        mod = mod,
        editRoute = editRoute,
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
            entity = galaxy,
            descriptor = "a galaxy",
            description = galaxy.description,
            mod = mod,
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
            cellGrid(listOf(starCell(media.username), postedAtCell(media.createdAt)), emptyList())

            media.text?.let {
                column(modify(Padding(2), AlignSelfCenter, MaxWidthTextBody, TextLarge)) {
                    markdown(it)
                }
            }
        }
    }
}