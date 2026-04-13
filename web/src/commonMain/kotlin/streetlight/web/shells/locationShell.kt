package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Location
import streetlight.web.pages.appFooter
import streetlight.web.ui.locationHeader

fun FlowContent.locationShell(
    location: Location
) {
    column(LocationShell.id) {
        locationHeader(location)
        row(modify(FlexItems1, AlignItemsStretch)) {
            card() {
                row(modify(AlignItemsEnd)) {
                    heading3(location.name, modify(Dim, MarginTop1, Flex1))
                }
                location.description?.let {
                    textBlock(it)
                }
                column(modify(Gap0, AlignItemsStretch)) {
                    location.address?.let {
                        propertyRow("address") {
                            textBlock(it)
                        }
                    }
                    propertyRow("phone") {
                        textBlock("(303) 555-2122")
                    }
                    propertyRow("email") {
                        textBlock("yer.email@gmail.com")
                    }
                    propertyRow("instagram") {
                        textBlock("@yer-instagram")
                    }
                    location.website?.let {
                        propertyRow("link") {
                            navigation(it, text = it)
                        }
                    }
                }
            }
            geoMapMount(location.geoPoint, modify(AspectRatio1))
        }
        wireBlock(LocationShell.adminCard)
        tabs(LocationShell.tabsId) {
            tab("events") {
                textBlock("yer events")
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

object LocationShell {
    val id = Id("location-shell")
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