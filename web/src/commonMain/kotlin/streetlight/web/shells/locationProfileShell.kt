package streetlight.web.shells

import kampfire.model.medium
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.Location
import streetlight.web.EditLocationIdRoute
import streetlight.web.layouts.cellCard
import streetlight.web.layouts.locationLightCell
import streetlight.web.layouts.postedByCell
import streetlight.web.layouts.propertyCell
import streetlight.web.pages.appFooter

fun FlowContent.locationProfileShell(
    location: Location
) {
    column(LocationProfileKey.Id, modify(AlignItemsStretch, Gap4)) {
        card(modify(ZenBg, BorderRadius2, Padding0, OverflowClip, Gap0, QueryContainer)) {
            column(modify(ContainerMdRow, FlexItems1, CardBg, Gap0)) {
                val imageUrl = location.images.medium
                if (imageUrl != null) {
                    featureImage(imageUrl, modify(Aspect3By2))
                }
                column(modify(JustifyContentCenter)) {
                    column(modify(PaddingX1, PaddingY2)) {
                        heading2(location.name, modify(TextAlignCenter, MinWidth0))
                        filigree(modify()) {
                            textBlock("at", modify(OpacityHalf))
                        }

                        heading4(location.addressLine, modify(OpacityMost, TextAlignCenter))
                    }
                }
            }
            row(modify(MinHeight8, FlexItems1, GapTiny, TextAlignCenter, WrapFlex, ZenBg, MoonShadow)) {
                cellCard {
                    postedByCell(location.username)
                }
                location.phone?.let {
                    cellCard {
                        propertyCell("phone", it)
                    }
                }
                location.email?.let {
                    cellCard {
                        propertyCell("email", it)
                    }
                }
                cellCard {
                    locationLightCell(location.locationId)
                }
                // td: instagram
                // propertyRow("instagram") {
                //     textBlock("@yer-instagram")
                // }
            }
            column(modify(ContainerMdRow, Padding4, Gap4, AlignItemsStart)) {
                column(modify(Flex4)) {
                    location.description?.let {
                        markdown(it)
                    }
                }
                row(modify(Flex1, WrapFlex, AlignItemsStart, FlexItems1)) {
                    location.links?.let { links ->
                        links.forEach { link ->
                            btn(link.label, link.url)
                        }
                    }
                    btn("edit", EditLocationIdRoute(location.locationId), modify(Secondary))
                }

            }
        }

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