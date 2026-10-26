package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import streetlight.compose.model.AppSettings
import streetlight.compose.model.HomeModel
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/** The view of [streetlight.compose.model.HomeRoute]: the map over the home feed's [feedSection], in masonry. */
context(model: HomeModel)
@Composable
fun HomeView() {
    val feed by model.feedState.collectAsState()
    val settings = koinInject<AppSettings>()
    val isLoading by model.isLoadingState.collectAsState()
    val problem by model.problemState.collectAsState()
    val theme = currentTheme

    val message = problem ?: if (isLoading) "Loading…" else null
    if (message != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BasicText(message, color = { theme.colors.ink })
        }
        return
    }

    val mode by settings.feedModeState.collectAsState()

    // in grid mode the grid holds its items a unit from the edges, in place of the map's own padding
    val isGrid = mode == FeedMode.Grid

    // the map is pinned, as the grid would otherwise dispose it when it scrolls away and start its renderer anew
    LazyStaggeredGridView(
        spacing = if (isGrid) theme.unit else theme.line,
        contentPadding = PaddingValues(if (isGrid) theme.unit else 0.dp),
    ) {
        pinnedItem {
            MapWindow(
                Modifier.fillMaxWidth()
                    .padding(if (isGrid) 0 else 1)
                    .height(theme.unit * MapHeightUnits)
                    .borderRadius(2, moonShadow = true, outline = Outline.Mid)
            )
        }
        feed?.let { feedSection(it, model.feedTypeState, mode, settings.feedModeState) }
    }
}

/** The height of the map in units, as on the web's home page. */
private const val MapHeightUnits = 48
