package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.model.ScaledImageArray
import kampfire.model.medium
import koala.SiteImage
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.StreetlightRoute

fun FlowContent.largePostCard(
    title: String,
    subtitle: String?,
    description: Markdown?,
    links: List<ExtraLink>?,
    images: ScaledImageArray?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute?,
    modifiers: ModifierSet? = null,
    cells: List<(FlowContent.() -> Unit)?>
) {
    val imageUrl = images?.medium ?: SiteImage.placeholderLg.url

    card(modify(modifiers, QueryContainer, Padding0, OverflowHidden, MoonShadow)) {
        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            // non-grid content
            column(modify(Flex3, ContainerMdRow, Gap0)) {
                featureImage(imageUrl, modify(Flex1, MinHeight24))
                column(modify(Flex2, Padding1, Height24, MaxHeight24)) {
                    row {
                        column(modify(Flex1, Gap0)) {
                            navigation(postRoute) {
                                heading3(title, modify(WhiteSpaceNoWrap, LineHeight1, MarginTop1, TextOverflowEllipses))
                            }

                            subtitle?.let {
                                fun FlowContent.showSubtitle() = textBlock(subtitle, modify(Dim))
                                when (subRoute) {
                                    null -> showSubtitle()
                                    else -> navigation(subRoute) { showSubtitle() }
                                }
                            }
                        }
                    }
                    description?.let { description ->
                        navigation(postRoute, modify(Flex1, SmallText, OverflowHidden, FadeBottom)) {
                            textBlock(description.value)
                        }
                    }

                    row {
                        links?.forEach { link ->
                            btn(link.label, link.url, modify(Secondary))
                        }
                    }
                }
            }

            if (cells.isNotEmpty()) {
                // grid content
                row(modify(Flex1, ContainerLgColumn, MinHeight8, FlexItems1, GapTiny, TextAlignCenter, FlexWrap, MoonShadow)) {
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



