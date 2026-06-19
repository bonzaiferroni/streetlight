package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.model.Url
import koala.css.AlignItemsStart
import koala.css.Aspect3By2
import koala.css.ContainerMdRow
import koala.css.Flex4
import koala.css.FlexBasisMin
import koala.css.FlexItems1
import koala.css.FlexWrap
import koala.css.Gap0
import koala.css.Gap4
import koala.css.Italic
import koala.css.JustifyContentCenter
import koala.css.LineHeight1
import koala.css.ModifierSet
import koala.css.MoonShadow
import koala.css.OpacityHalf
import koala.css.OpacityHigh
import koala.css.OverflowClip
import koala.css.Padding0
import koala.css.Padding2
import koala.css.Padding4
import koala.css.Property
import koala.css.QueryContainer
import koala.css.TextAlignCenter
import koala.css.VignetteOver
import koala.css.Zen
import koala.css.ZenBg
import koala.css.modify
import koala.css.setStyle
import koala.html.AppRoute
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading2
import koala.html.heading4
import koala.html.markdown
import koala.html.row
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellBlock

fun FlowContent.featureHeader(
    title: String,
    descriptor: String,
    subtitle: String?,
    image: Url?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    description: Markdown? = null,
    cellContent: (FlowContent.() -> Unit)? = null,
    links: List<ExtraLink>? = null,
    editRoute: AppRoute? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) {
    card(modify(modifiers, QueryContainer, Padding0, Gap0, OverflowClip, MoonShadow, ZenBg)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
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