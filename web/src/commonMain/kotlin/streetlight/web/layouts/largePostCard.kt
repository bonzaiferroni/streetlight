package streetlight.web.layouts

import kampfire.api.Markdown
import koala.Image
import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.model.ui.StreetlightRoute

fun FlowContent.largePostCard(
    title: String,
    subtitle: String?,
    description: Markdown?,
    links: List<ExtraLink>?,
    image: Image?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute?,
    mod: Modifier? = null,
    cells: List<(FlowContent.() -> Unit)?>
) {
    card(modify(mod, ContainerTypeInlineSize, Padding(0), OverflowHidden, MoonShadow)) {
        column(modify(ContainerTypeInlineSize, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                featureImage(image, modify(Flex1, MinHeight(24)))
                column(modify(Flex2, Padding(1), Height(24), MaxHeight(24))) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            navigation(postRoute) {
                                heading3(title, modify(WhiteSpaceNoWrap, LineHeight1, MarginTop(1), TextOverflowEllipses))
                            }

                            subtitle?.let {
                                fun FlowContent.showSubtitle() = textBlock(subtitle, InkDimFg)
                                when (subRoute) {
                                    null -> showSubtitle()
                                    else -> navigation(subRoute) { showSubtitle() }
                                }
                            }
                        }
                    }
                    description?.let { description ->
                        navigation(postRoute, modify(Flex1, TextSmall, OverflowHidden, FadeBottom)) {
                            textBlock(description.value)
                        }
                    }

                    row {
                        links?.forEach { link ->
                            btn(link.label, link.url, Secondary)
                        }
                    }
                }
            }

            if (cells.isNotEmpty()) {
                // grid content
                row(modify(Flex1, ContainerLgColumn, MinHeight(8), FlexItems1, Gap2Px, TextAlignCenter, FlexWrap, MoonShadow)) {
                    cells.forEach {
                        val cell = it ?: return@forEach
                        cell {
                            cell()
                        }
                    }
                }
            }
        }
    }
}



