package streetlight.compose.model

import koala.SvgFile
import streetlight.compose.view.FeedMode
import streetlight.model.data.FeedType

fun FeedMode.toSvg() = when (this) {
    FeedMode.Minimal -> SvgFile.LayoutMinimal.small
    FeedMode.Row -> SvgFile.LayoutRow.small
    FeedMode.Grid -> SvgFile.LayoutGrid.small
}

/** Whether a feed of this type has a search field, as the web's. */
val FeedType.isSearched get() = when (this) {
    FeedType.Events, FeedType.StarGalaxies, FeedType.TopGalaxies, FeedType.NewGalaxies, FeedType.Cities, FeedType.Stars -> true
    FeedType.Locations, FeedType.GalaxyPosts, FeedType.Posts, FeedType.Media -> false
}
