package streetlight.model.data

import kotlinx.serialization.Serializable

/** A location with its page design. */
@Serializable
data class LocationLayout(
    val location: Location,
    val design: PageDesign?,
)

/** The layout of each kind of page that has no design of its own. */
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
    val media = PageLayout(
        blocks = listOf(
            HeaderBlock,
            CommentsBlock,
        )
    )
    val star = PageLayout(
        blocks = listOf(
            HeaderBlock,
            PostsBlock,
        )
    )
    val galaxy = PageLayout(
        blocks = listOf(
            HeaderBlock,
            PostsBlock,
        )
    )
}