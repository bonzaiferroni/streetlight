package streetlight.compose.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
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

/** The screen of [route]: its location once inflated, shown in the [LocationView]. */
@Composable
fun LocationScreen(route: LocationRoute) = Inflated<LocationContent>(route) { content ->
    context(koinViewModel<LocationModel> { parametersOf(content) }) { LocationView() }
}

/**
 * The view of [LocationRoute]: the header, then the location's page layout, in the location's own theme, and the
 * footer. The cousin of the web's `locationShell`.
 */
@Composable
context(model: LocationModel)
fun LocationView() {
    val page by model.feed.feedState.collectAsState()
    val mode by model.feed.modeState.collectAsState()
    val theme = currentTheme
    PageThemeEffect(model.content.design?.theme)

    // in grid mode the grid holds its items a unit from the edges, in place of their own padding
    val isGrid = mode == FeedMode.Grid
    LazyStaggeredGridView(
        spacing = if (isGrid) theme.unit else theme.line,
        contentPadding = PaddingValues(if (isGrid) theme.unit else 0.dp),
    ) {
        fullItem { AppHeader() }
        context(model.feed) { renderLayout(model.content, page, mode, side = if (isGrid) 0 else 1) }
        fullItem { AppFooter(LocationViewSource, Modifier.padding(top = 8)) }
    }
}

/** The path of this view's source, linked from its footer. */
private const val LocationViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/LocationView.kt"
