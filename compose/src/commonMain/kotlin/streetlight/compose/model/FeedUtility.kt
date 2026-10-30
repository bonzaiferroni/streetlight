package streetlight.compose.model

import koala.SvgFile
import streetlight.compose.view.FeedMode
import streetlight.model.data.FeedType

/** The heading of a feed of this type. */
val FeedType.heading get() = when (this) {
    FeedType.Events -> "Events"
    FeedType.Locations -> "Locations"
    FeedType.GalaxyPosts, FeedType.Posts -> "Posts"
    FeedType.Media -> "Media"
    FeedType.StarGalaxies -> "My Galaxies"
    FeedType.TopGalaxies -> "Top Galaxies"
    FeedType.NewGalaxies -> "New Galaxies"
    FeedType.Cities -> "Cities"
    FeedType.Stars -> "Stars"
}

fun FeedType.toSvg() = when (this) {
    FeedType.Events -> SvgFile.Calendar.small
    FeedType.Locations -> SvgFile.MapPin.small
    FeedType.GalaxyPosts -> SvgFile.Planet.small
    FeedType.Posts -> SvgFile.News.small
    FeedType.Media -> SvgFile.Photo.small
    FeedType.StarGalaxies -> SvgFile.Star.small
    FeedType.TopGalaxies -> SvgFile.Trophy.small
    FeedType.NewGalaxies -> SvgFile.Seedling.small
    FeedType.Cities -> SvgFile.City.small
    FeedType.Stars -> SvgFile.Star.small
}

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
