package streetlight.web.shells

import kabinet.utils.toAgoFormat
import kampfire.model.medium
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.StreetlightRoute
import streetlight.web.layouts.cellRow
import streetlight.web.layouts.eventCells
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationCells
import streetlight.web.layouts.locationRoute
import streetlight.web.layouts.route

fun FlowContent.layoutPosts(posts: List<Post>) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        column(modify(Gap2)) {
            posts.forEach { post ->
                when (post) {
                    is EventPost -> {
                        val event = post.event ?: return@forEach
                        postRow(
                            post = post,
                            subtitle = "${event.locationName}, ${event.city}",
                            postRoute = event.eventRoute,
                            subRoute = event.locationRoute,
                            cells = eventCells(event)
                        )
                    }
                    is LocationPost -> {
                        val location = post.location ?: return@forEach
                        postRow(
                            post = post,
                            subtitle = location.addressLine,
                            postRoute = location.route,
                            subRoute = null,
                            cells = locationCells(post.location),
                        )
                    }
                }
            }
        }
    }
}

fun FlowContent.postRow(
    post: Post,
    subtitle: String?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute?,
    cells: List<(FlowContent.() -> Unit)?>? = null
) {
    val colorScheme = colorSchemeOf(post.postType)
    val flairIcon = flairIconOf(post.postType)

    column {
        card(modify(QueryContainer, Padding0, OverflowClip, ZenBg, MoonShadow)) {
            setStyle(Property.ColorScheme.to(colorScheme))

            column(modify(QueryContainer, ContainerLgRow, Gap0)) {

                row(modify(Flex1, Gap0, Height24)) {

                    // image
                    navigation(postRoute, modify(Height24, Aspect1)) {
                        featureImage(post.images.medium, modify(Size100P))
                    }

                    // middle column
                    column(modify(Flex1, Padding1, Height24, PositionRelative)) {
                        flairIcon?.let {
                            icon(it, modify(Height9, PositionAbsolute, Top0, Right0, OpacityGhost, ColorSchemeFg))
                        }

                        column(modify(Gap0)) {
                            navigation(postRoute) {
                                heading3(post.title, modify(LineHeight1, SingleLine, Bold, Shrinkable, Flex1, MarginTop1))
                            }
                            subtitle?.let {
                                navigationIfNotNull(subRoute) {
                                    textBlock(subtitle, modify(OpacityMost))
                                }
                            }
                        }
                        post.description?.let {
                            navigation(postRoute, modify(Flex1, OverflowHidden, FadeBottom)) {
                                textBlock(it, modify(SmallText))
                            }
                        }

                        // button row
                        row(modify(AlignItemsCenter)) {
                            row(modify(Flex1, OverflowXAuto)) {
                                post.links?.forEach { link ->
                                    btn(link.label, link.url, modify(Zen))
                                }
                            }
                            icon(SvgFile.MapPin, modify(Height4, MarginRight1))
                        }
                    }
                }

                // cell content
                if (cells != null) {
                    cellRow(cells, modify(ContainerLgColumn, FlexWrap))
                }
            }
        }
        row(modify(JustifyContentEnd, MarginRight2)) {
            textBlock(modifiers = modify(SmallText, LineHeight1, OpacityMost)) {
                span("— posted by ")
                span(post.username ?: "Someone", modify(Bold))
                span(" ${post.createdAt.toAgoFormat()}")
            }
        }
    }
}

fun colorSchemeOf(postType: PostType) = when (postType) {
    PostType.Event -> "var(--accent-fg)"
    else -> "var(--primary-fg)"
}

fun flairIconOf(postType: PostType) = when (postType) {
    PostType.Event -> SvgFile.Calendar
    PostType.Location -> SvgFile.Pin
    else -> null
}