package streetlight.web.shells

import koala.css.AlignItemsStart
import koala.css.Dim
import koala.css.FlexItems1
import koala.css.QueryMediumRow
import koala.css.modify
import koala.html.column
import koala.html.heading3
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.web.pages.appFooter

fun FlowContent.spotlightTab(content: SpotlightContent) {
    val galaxies = content.galaxies; val posts = content.posts
    column {
        column {
            heading3("Galaxies")
            textBlock("Galaxies are Streetlight communities, each with a particular focus.", modify(Dim))
            galaxies.forEach { galaxy ->
                cardOf(galaxy)
            }
        }
        column(modify(QueryMediumRow, FlexItems1, AlignItemsStart)) {
            column {
                heading3("Posts")
                posts.forEach { post ->
                    cardOf(post)
                }
            }
        }

        appFooter()
    }
}

data class SpotlightContent(
    val galaxies: List<Galaxy>,
    val posts: List<GalaxyPost>
)