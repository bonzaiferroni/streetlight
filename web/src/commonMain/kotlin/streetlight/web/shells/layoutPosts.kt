package streetlight.web.shells

import kabinet.utils.toAgoFormat
import kampfire.model.medium
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.StreetlightRoute
import streetlight.web.layouts.cellCard
import streetlight.web.layouts.costCell
import streetlight.web.layouts.eventLightCell
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationLightCell
import streetlight.web.layouts.locationRoute
import streetlight.web.layouts.postedByCell
import streetlight.web.layouts.route
import streetlight.web.layouts.startsAtCell

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
                            cells = listOf(
                                { startsAtCell(event.startsAt) },
                                { costCell(event.cost, event.url) },
                                { postedByCell(post.username) },
                                { eventLightCell(event.lightCount, event.eventId) },
                            )
                        )
                    }
                    is LocationPost -> {
                        val location = post.location ?: return@forEach
                        postRow(
                            post = post,
                            subtitle = location.addressLine,
                            postRoute = location.route,
                            subRoute = null,
                            cells = listOf(
                                { postedByCell(post.username) },
                                { locationLightCell(location.lightCount, location.locationId)}
                            )
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
    cells: List<(FlowContent.() -> Unit)?>
) {
    val colorScheme = colorSchemeOf(post.postType)

    column {
        card(modify(QueryContainer, Padding0, OverflowClip, ZenBg, MoonShadow)) {
            colorScheme?.let {
                setStyle(Property.ColorScheme.with(colorScheme))
            }

            column(modify(QueryContainer, ContainerLgRow, Gap0)) {

                row(modify(Flex1, Gap0, Height24)) {
                    navigation(postRoute, modify(Height24, Aspect1)) {
                        featureImage(post.images.medium, modify(Size100P))
                    }

                    column(modify(Flex1, Padding1, Height24)) {
                        column(modify(Gap0)) {
                            navigation(postRoute) {
                                row(modify(JustifyContentSpaceBetween, MarginTop1, AlignItemsStart)) {
                                    heading3(post.title, modify(LineHeight1, SingleLine, Bold, Shrinkable, Flex1))
                                    textBlock(post.postType.label, modify(LineHeight1, SingleLine, ColorSchemeFg, MarginRight1))
                                }
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

                if (cells.isNotEmpty()) {
                    // grid content
                    row(modify(ContainerLgColumn, MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex, MoonShadow, MinWidth24)) {
                        cells.forEach {
                            val cell = it ?: return@forEach
                            cellCard {
                                cell()
                            }
                        }
                    }
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