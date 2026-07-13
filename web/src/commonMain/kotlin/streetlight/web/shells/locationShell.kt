package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.layouts.layoutPosts
import streetlight.web.layouts.postRow
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.headerOf

fun FlowContent.locationShell(
    content: LocationContent,
) {
    column(LocationProfileKey.Id, BodyStyle.Mod) {
        headerOf(
            location = content.location,
            editRoute = if (content.canEdit) LocationUpdateRoute(content.location.slug) else null
        )

        tabs(LocationProfileKey.tabsId) {
            if (content.events.isNotEmpty()) {
                tab("events") {
                    layoutPosts {
                        content.events.forEach {
                            postRow(it)
                        }
                    }
                }
            }
            tab("directions") {
                textBlock("yer directions")
            }
            tab("menu") {
                textBlock("yer menu")
            }
            tab("talk") {
                textBlock("yer talk")
            }
        }
        appFooter()
    }
}

object LocationProfileKey {
    val Id = Id("location-shell")
    val tabsId = Id("location-tabs")
    val adminCard = Id("location-admin-card")
}

fun FlowContent.propertyRow(property: String, block: DIV.() -> Unit) {
    row {
        textBlock("${property}:", modify(Flex1, OpacityHigh, TextAlignRight))
        box(modify(Flex2), block = block)
    }
}

//    val locationId: LocationId,
//    val hostId: UserId?,
//    val name: String,
//    val description: String?,
//    val address: String?,
//    val notes: String?,
//    val geoPoint: GeoPoint,
//    val resources: Set<ResourceType>,
//    val link: String?,
//    val eventsLink: String?,
//    val imageUrl: String?,
//    val thumbUrl: String?,
//    val checkedAt: Instant?,
//    val updatedAt: Instant,
//    val createdAt: Instant,