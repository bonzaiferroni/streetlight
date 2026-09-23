package streetlight.web.ui

import kampfire.api.Markdown
import koala.Image
import koala.modifier.*
import koala.html.AppRoute
import koala.html.card
import koala.html.column
import koala.html.filigree
import koala.html.heading2
import koala.html.metaImage
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.layouts.EntityButton
import streetlight.web.layouts.EntityCell
import streetlight.web.layouts.ThemeColor
import streetlight.web.layouts.cellGrid
import streetlight.web.layouts.entityBody

fun FlowContent.pageHeader(
    title: String,
    descriptor: String,
    image: Image?,
    subtitle: String? = null,
    colorScheme: ThemeColor = ThemeColor.Primary,
    description: Markdown? = null,
    cells: List<EntityCell>? = null,
    buttons: List<EntityButton>? = null,
    links: List<ExtraLink>? = null,
    editRoute: AppRoute? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    card(modify(mod, ContainerTypeInlineSize, Padding(0), Gap0, OverflowClip, MoonShadow, ZenBg, BorderSolid2Px)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        block()

        column(modify(ContainerMdRow, FlexItems1, Gap0)) {
            // image
            image?.let {
                metaImage(image, modify(VignetteOver, MinWidth(0)))
            }

            // title panel
            column(modify(JustifyContentCenter, ZenBg)) { // necessary for flex1 because the inner column has padding
                column(Padding(2)) {
                    heading2(title, modify(TextAlignCenter, LineHeight1, FocusTarget))
                    filigree { textBlock(descriptor, modify(Italic, OpacityHalf)) }
                    subtitle?.let {
                        textBlock(subtitle, modify(OpacityHigh, TextAlignCenter))
                    }
                }
            }
        }

        // cell content
        cellGrid(cells, buttons)

        entityBody(description, links, editRoute)
    }
}