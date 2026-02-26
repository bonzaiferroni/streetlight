package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.coroutines.launch
import kotlinx.html.FlowContent
import streetlight.model.data.Location
import streetlight.model.data.toLocationEdit
import streetlight.web.EditLocationDataRoute
import streetlight.web.EditLocationIdRoute

fun FlowContent.locationShell(
    location: Location
) {
    column {
//        location.imageUrl?.let {
//            image(it, modify(MaxHeight64, MarginAuto))
//        }
//        heading1(location.name)
        headerImage(location.name, location.imageUrl)
        row(modify(FlexItems1, AlignItemsStretch)) {
            card {
                location.link?.let {
                    row {
                        propertyValue("link", it, modify(Flex1))
                    }
                }
                button("Edit", EditLocationIdRoute(location.locationId))
            }
            geoMapMount(modify(Square))
        }
    }
}