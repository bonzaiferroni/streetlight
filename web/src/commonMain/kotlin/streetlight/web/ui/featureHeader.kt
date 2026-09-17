package streetlight.web.ui

import kampfire.api.Markdown
import koala.Image
import koala.modifier.*
import koala.html.AppRoute
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.filigree
import koala.html.heading2
import koala.html.heading4
import koala.html.markdown
import koala.html.metaImage
import koala.html.row
import koala.html.textBlock
import koala.modifier.Css
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.layouts.ThemeColor
import streetlight.web.layouts.cellBlock

fun FlowContent.featureHeader(
    title: String,
    descriptor: String,
    image: Image?,
    subtitle: String? = null,
    colorScheme: ThemeColor = ThemeColor.Primary,
    description: Markdown? = null,
    cellContent: (FlowContent.() -> Unit)? = null,
    links: List<ExtraLink>? = null,
    editRoute: AppRoute? = null,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) {
    card(modify(mod, QueryContainer, Padding0, Gap0, OverflowClip, MoonShadow, ZenBg, BorderSolid2Px)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        block()

        column(modify(ContainerMdRow, FlexItems1, Gap0)) {
            // image
            image?.let {
                metaImage(image, modify(VignetteOver, MinWidth(0)))
            }

            // title panel
            column(modify(JustifyContentCenter, ZenBg)) { // necessary for flex1 because the inner column has padding
                column(modify(Padding2)) {
                    heading2(title, modify(TextAlignCenter, LineHeight1, FocusTarget))
                    filigree { textBlock(descriptor, modify(Italic, OpacityHalf)) }
                    subtitle?.let {
                        heading4(subtitle, modify(OpacityHigh, TextAlignCenter))
                    }
                }
            }
        }

        // cell content
        if (cellContent != null) {
            cellBlock(block = cellContent)
        }

        if (description != null || links != null ) {
            column(modify(ContainerMdRow, Padding4, Gap(4), AlignItemsStart)) {

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