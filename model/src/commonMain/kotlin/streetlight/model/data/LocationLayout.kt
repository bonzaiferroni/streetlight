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
            HeaderBlock(),
            EventsBlock,
        ),
    )
    val media = PageLayout(
        blocks = listOf(
            HeaderBlock(),
            CommentsBlock,
        )
    )
    val star = PageLayout(
        blocks = listOf(
            HeaderBlock(),
            SkillsBlock,
            PostsBlock,
        )
    )
    val galaxy = PageLayout(
        blocks = listOf(
            HeaderBlock(),
            PostsBlock,
        )
    )
}