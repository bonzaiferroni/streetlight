package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.html.action
import koala.html.card
import koala.html.column
import koala.html.heading4
import koala.html.heading5
import koala.html.icon
import koala.html.image
import koala.html.row
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.web.EventSlugRoute
import streetlight.web.LocationIdRoute

fun FlowContent.smallPostCard(post: EventPost) {
    val thumbUrl = post.images.thumb
    val title = post.title
    val description = post.description
    val event = post.event ?: return // td: show removed content
    val postRoute = event.eventRoute

    card(modify(Padding0, OverflowHidden)) {
        column(modify(MediaLgRow, AlignItemsStretch, Gap0)) {
            row(modify(Height16, AlignItemsStart, Padding1)) {
                thumbUrl?.let {
                    image(thumbUrl, modify(Height100P, AspectRatio1, BorderRadius1))
                }
                column(modify(Flex1, Height100P)) {
                    row(modify(AlignItemsStart)) {
                        column(modify(Flex1, Gap0)) {
                            action(postRoute) {
                                heading5(title)
                            }
                            action(event.locationRoute) {
                                textBlock(event.locationName, modify(Dim))
                            }
                        }
                        icon(SvgFile.Focus, modify(Height5, AspectRatio1, Dim))
                    }
                    description?.let {
                        action(postRoute) {
                            textBlock(description, modify(Flex1, SmallText, Height2, OverflowHidden))
                        }
                    }
                }
            }
            row(modify(MinHeight8, FlexItems1, AlignItemsStretch, GapTiny, TextAlignCenter)) {
                val cellModifiers = modify(AlignItemsCenter, Gap0, BorderRadius0, JustifyContentCenter)
//                card(cellModifiers) {
//                    post.location?.let { location ->
//                        textBlock(location.name)
//                        textBlock("5.2 miles", modify(Dim))
//                    }
//                }
                card(cellModifiers) {
                    post.event?.startsAt?.let { startsAt ->
                        heading4(startsAt.toRelativeDayFormat())
                        textBlock("8:00 PM")
                    }
                }
                card(cellModifiers) {
                    post.event?.cost?.let {
                        textBlock("Tickets", modify(Dim, SmallText))
                        textBlock("$$it")
                    }
                }
                post.event?.let { event ->
                    card(cellModifiers) {
                        // val interest = EventStar(event.eventId, post.interest)
                        lightCell(event.eventId)
                    }
                }
            }
        }
    }
}

// td: find better home
val Location.route get() = LocationIdRoute(locationId)
val Event.route get() = EventSlugRoute(slug)
val EventLocation.eventRoute get() = EventSlugRoute(slug)
val EventLocation.locationRoute get() = LocationIdRoute(locationId)