package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.model.*
import streetlight.web.shells.cardOf

fun RenderContext.viewScoutMap(app: AppContext) {
    val locationsFlow = app.streetMap.locationsFlow

    renderScope.launch {
        locationsFlow.collect {
            console.log("locations ${it.size}")
        }
    }

    column {
        viewGeoMap(app.geoMap, app.appScope)

        card {
            row {
                textField("link", modify(Flex1))
                button("🤖 read link")
                button("📝 editor")
            }
        }

        card {
            itemsBlock(locationsFlow, modify(Blur, SlideX), magic = true) { (location, events) ->
                box {
                    cardOf(location)
                }
            }
        }
    }
}
