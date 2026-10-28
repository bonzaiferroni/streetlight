package streetlight.compose.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.LocationModel
import streetlight.compose.model.LocationRoute
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.LocationContent

/** The screen of [route]: the logo as its top bar, and its location once inflated, shown in the [LocationView]. */
@Composable
fun LocationScreen(route: LocationRoute) {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, remember(gridState) { gridState.scrolledUnder() })
    Inflated<LocationContent>(route) { content ->
        context(koinViewModel<LocationModel> { parametersOf(content) }) { LocationView(gridState) }
    }
}

/**
 * The view of [LocationRoute], in a grid scrolled by [gridState] under the top bar: the location's page layout, in
 * the location's own theme, and the footer. The cousin of the web's `locationShell`.
 */
@Composable
context(model: LocationModel)
fun LocationView(gridState: LazyStaggeredGridState) {
    val page by model.feed.feedState.collectAsState()
    val mode by model.feed.modeState.collectAsState()
    val theme = currentTheme
    PageThemeEffect(model.content.design?.theme)

    // in grid mode the grid holds its items a unit from the edges, in place of their own padding
    val isGrid = mode == FeedMode.Grid
    val edge = if (isGrid) theme.unit else 0.dp
    LazyStaggeredGridView(
        state = gridState,
        spacing = if (isGrid) theme.unit else theme.line,
        contentPadding = PaddingValues(start = edge, top = theme.unit * TopBarUnits + edge, end = edge, bottom = edge),
    ) {
        context(model.feed) { renderLayout(model.content, page, mode, side = if (isGrid) 0 else 1) }
        fullItem { AppFooter(LocationViewSource, Modifier.padding(top = 8)) }
    }
}

/** The path of this view's source, linked from its footer. */
private const val LocationViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/LocationScreen.kt"
