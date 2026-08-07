package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationLayout(
    val location: Location,
    val layout: Layout?,
)

object DefaultLayout {
    val location = Layout(
        blocks = listOf(
            HeaderBlock,
            TabsBlock(listOf(
                TabContent("events", listOf(EventsBlock)),
                TabContent("directions", listOf(MapBlock))
            )),
            FooterBlock,
        ),
    )
}