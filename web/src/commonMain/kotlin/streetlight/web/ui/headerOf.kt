package streetlight.web.ui

import kampfire.model.Url
import kampfire.model.large
import kampfire.model.medium
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Star
import streetlight.web.EditEventIdRoute
import streetlight.web.layouts.cellRow
import streetlight.web.layouts.galaxyCells

fun FlowContent.headerOf(
    location: Location,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(location.name, location.images.medium, modifiers, block)
}

fun FlowContent.headerOf(
    galaxy: Galaxy,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerOf(
        title = galaxy.name,
        descriptor = "a galaxy",
        subtitle = galaxy.tagline,
        image = galaxy.images.large,
        description = galaxy.description,
        modifiers = modifiers,
        cells = galaxyCells(galaxy),
        block = block
    )
}

fun FlowContent.headerOf(
    star: Star,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    headerImage(star.username, star.images.medium, modifiers, block)
}

fun FlowContent.headerOf(
    title: String,
    descriptor: String,
    subtitle: String?,
    image: Url?,
    description: String? = null,
    cells: List<(FlowContent.() -> Unit)?>? = null,
    links: List<ExtraLink>? = null,
    editRoute: AppRoute? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) {
    card(modify(modifiers, QueryContainer, Padding0, Gap0, OverflowClip, MoonShadow, ZenBg)) {
        block()

        column(modify(ContainerMdRow, FlexItems1, Gap0)) {
            // image
            featureImage(image, modify(Aspect3By2, VignetteOver))

            // title panel
            column(modify(JustifyContentCenter, ZenBg)) { // necessary for flex1 because the inner column has padding
                column(modify(Padding2)) {
                    heading2(title, modify(TextAlignCenter, LineHeight1))
                    filigree { textBlock(descriptor, modify(Italic, OpacityHalf)) }
                    subtitle?.let {
                        heading4(subtitle, modify(OpacityMost, TextAlignCenter))
                    }
                }
            }
        }

        // cell content
        if (cells != null) {
            cellRow(cells)
        }

        if (description != null || links != null ) {
            column(modify(ContainerMdRow, Padding4, Gap4, AlignItemsStart)) {

                // description
                description?.let {
                    column(modify(Flex4)) {
                        markdown(description)
                    }
                }

                // button row
                links?.let { links ->
                    row(modify(FlexWrap, FlexBasisMin, AlignItemsStart)) {
                        links.forEach { link ->
                            btn(link.label, link.url, modify(Zen))
                        }
                        editRoute?.let {
                            btn("edit", editRoute, modify(Zen))
                        }
                    }
                }
            }
        }
    }
}