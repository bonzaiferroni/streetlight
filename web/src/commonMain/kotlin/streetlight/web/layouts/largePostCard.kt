package streetlight.web.layouts

import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Entity

fun FlowContent.largePostCard(
    entity: Entity,
    mod: Modifier? = null,
) {
    val title = entity.label
    val subtitle = entity.toSubtitle()
    val description = entity.body
    val links = entity.links
    val image = entity.image
    val postRoute = entity.toRoute()
    val subRoute = entity.toSubRoute()

    card(modify(mod, ContainerTypeInlineSize, Padding(0), OverflowHidden, MoonShadow)) {
        column(modify(ContainerTypeInlineSize, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                featureImage(image, modify(Flex1, MinHeight(24)))
                column(modify(Flex2, Padding(1), Height(24), MaxHeight(24))) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            navigationIfNotNull(postRoute) {
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
                        navigationIfNotNull(postRoute, modify(Flex1, TextSmall, OverflowHidden, FadeBottom)) {
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

            // grid content
            cellGrid(entity.toCells(), entityButtonsOf(entity, false), modify(Flex1, ContainerLgColumn, MinHeight(8)))
        }
    }
}



