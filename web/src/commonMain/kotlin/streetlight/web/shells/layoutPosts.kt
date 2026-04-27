package streetlight.web.shells

import kampfire.model.ScaledImageArray
import kampfire.model.medium
import koala.SiteImage
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.StreetlightRoute
import streetlight.web.layouts.cellCard
import streetlight.web.layouts.costCell
import streetlight.web.layouts.eventLightCell
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationRoute
import streetlight.web.layouts.postedByCell
import streetlight.web.layouts.startsAtCell

fun FlowContent.layoutPosts(posts: List<Post>) {
    posts.forEach { post ->
        when (post) {
            is EventPost -> {
                val event = post.event ?: return@forEach
                postRow(
                    title = post.title,
                    subtitle = "${event.locationName}, ${event.city}",
                    description = post.description,
                    links = event.links,
                    images = post.images,
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
            is LocationPost -> TODO()
        }
    }
}

fun FlowContent.postRow(
    title: String,
    subtitle: String?,
    description: String?,
    links: List<ExtraLink>?,
    images: ScaledImageArray?,
    postRoute: StreetlightRoute,
    subRoute: StreetlightRoute?,
    cells: List<(FlowContent.() -> Unit)?>
) {

    card(modify(QueryContainer, Padding0, OverflowClip, ZenCardBg)) {
        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            row(modify(Flex1, Gap0, Height24)) {
                featureImage(images.medium, modify(Height24, Aspect1))

                column(modify(Flex1, Padding1, Height24)) {
                    column(modify(Gap0)) {
                        navigation(postRoute) {
                            heading2(title, modify(LineHeight1, MarginTop1, SingleLine, Bold))
                        }
                        subtitle?.let {
                            navigationIfNotNull(subRoute) {
                                textBlock(subtitle, modify(OpacityMost))
                            }
                        }
                    }
                    description?.let {
                        navigation(postRoute, modify(Flex1, OverflowHidden, FadeBottom)) {
                            textBlock(description, modify(SmallText))
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
}