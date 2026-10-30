package streetlight.compose.view

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import koala.Image
import koala.SiteImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.CityFeedRoute
import streetlight.compose.model.FeedPageModel
import streetlight.compose.model.GalaxyFeedRoute
import streetlight.compose.model.StarFeedRoute
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.CityFeedContent
import streetlight.model.data.GalaxyFeedContent
import streetlight.model.data.StarFeedContent
import streetlight.model.ui.TopBar

/** The screen of [GalaxyFeedRoute]: the logo as its top bar, and the feed of galaxies once inflated. */
@Composable
fun GalaxyFeedScreen() {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, gridState)
    Inflated<GalaxyFeedContent>(GalaxyFeedRoute) { content ->
        context(koinViewModel<FeedPageModel> { parametersOf(content.feed) }) {
            FeedPageView(gridState, "Galaxies", "Streetlight Communities", SiteImage.HelixNebula)
        }
    }
}

/** The screen of [CityFeedRoute]: the logo as its top bar, and the feed of cities once inflated. */
@Composable
fun CityFeedScreen() {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, gridState)
    Inflated<CityFeedContent>(CityFeedRoute) { content ->
        context(koinViewModel<FeedPageModel> { parametersOf(content.feed) }) {
            FeedPageView(gridState, "Cities", "Streetlight Cities", SiteImage.PearlStreet)
        }
    }
}

/** The screen of [StarFeedRoute]: the logo as its top bar, and the feed of stars once inflated. */
@Composable
fun StarFeedScreen() {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, gridState)
    Inflated<StarFeedContent>(StarFeedRoute) { content ->
        context(koinViewModel<FeedPageModel> { parametersOf(content.feed) }) {
            FeedPageView(gridState, "Stars", "Streetlight Stars", SiteImage.HelixNebula)
        }
    }
}

/**
 * The view of a page of one feed, in a grid scrolled by [gridState] under the top bar: a [PageHeader] of [title],
 * [descriptor] and [image], the feed's [feedSection], and the footer. The cousin of the web's `galaxyFeedShell`,
 * `cityFeedShell` and `starFeedShell`.
 */
@Composable
context(model: FeedPageModel)
fun FeedPageView(gridState: LazyStaggeredGridState, title: String, descriptor: String, image: Image?) {
    val page by model.feed.feedState.collectAsState()
    val mode by model.feed.modeState.collectAsState()
    PageThemeEffect(null)

    LazyStaggeredGridView(gridState, mode) {
        fullItem { PageHeader(title, descriptor, image, Modifier.padding(horizontal = mode.sideUnits, vertical = 1)) }
        context(model.feed) { feedSection(page, mode) }
        fullItem { AppFooter(FeedPageViewSource, Modifier.padding(top = 8)) }
    }
}

private const val FeedPageViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/FeedPageScreen.kt"
