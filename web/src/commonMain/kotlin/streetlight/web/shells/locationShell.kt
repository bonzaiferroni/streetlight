package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Location
import streetlight.model.data.toLocationEdit
import streetlight.web.EditLocationDataRoute
import streetlight.web.EditLocationIdRoute

fun FlowContent.locationShell(
    location: Location
) {
    column(LocationShell.id) {
        headerImage(location.name, location.imageUrl)
        row(modify(FlexItems1, AlignItemsStretch)) {
            card() {
                heading3(location.name, modify(Dim, MarginTop1))
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
                    location.link?.let {
                        propertyRow("link") {
                            action(it, it)
                        }
                    }
                }
                button("Edit", EditLocationIdRoute(location.locationId))
            }
            geoMapMount(location.geoPoint, modify(Square))
        }
    }
}

object LocationShell {
    val id = Id("location-shell")
}

fun FlowContent.propertyRow(property: String, block: DIV.() -> Unit) {
    row {
        textBlock("${property}:", modify(Flex1, Opacity6, TextAlignRight))
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