package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationLayout(
    val location: Location,
    val design: PageDesign?,
)

object DefaultLayout {
    val location = PageLayout(
        blocks = listOf(
            HeaderBlock,
            TabsBlock(listOf(
                TabContent("events", listOf(EventsBlock)),
                TabContent("directions", listOf(MapBlock))
            )),
        ),
    )
}