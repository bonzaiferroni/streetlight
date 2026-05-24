package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Location
import streetlight.web.pages.appFooter
import streetlight.web.ui.headerOf

fun FlowContent.locationShell(
    location: Location
) {
    column(LocationProfileKey.Id, modify(AlignItemsStretch, Gap4)) {
        headerOf(location)

        // td: add map

        // td: tab content
        // tabs(LocationProfileKey.tabsId) {
        //     tab("events") {
        //         textBlock("yer events")
        //     }
        //     tab("menu") {
        //         textBlock("yer menu")
        //     }
        //     tab("talk") {
        //         textBlock("yer talk")
        //     }
        // }
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
        textBlock("${property}:", modify(Flex1, OpacityMost, TextAlignRight))
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