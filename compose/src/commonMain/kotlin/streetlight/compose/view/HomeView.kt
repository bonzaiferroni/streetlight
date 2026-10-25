package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import streetlight.compose.model.AppSettings
import streetlight.compose.model.HomeModel
import streetlight.compose.modifier.borderRadius
import streetlight.compose.utility.collectAsState

/** The view of [streetlight.compose.model.HomeRoute]: the map over the home feed's [feedSection]. */
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

    val gridState = rememberLazyGridState()
    val mode by settings.feedModeState.collectAsState()
    val mapHeight = theme.unit * MapHeightUnits

    Box(Modifier.fillMaxSize()) {
        // the parts stand a line apart on their quiet background, so the theme behind draws the line between them
        LazyVerticalGrid(
            columns = GridCells.Adaptive(GridColumnWidth),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            verticalArrangement = Arrangement.spacedBy(theme.line),
            horizontalArrangement = Arrangement.spacedBy(theme.line),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.fillMaxWidth().padding(theme.unit).height(mapHeight)) }
            feed?.let { feedSection(it, model.feedTypeState, mode, settings.feedModeState) }
        }

        // the map stays out of the grid, which would dispose it when it scrolls away and start its renderer anew when
        // it returns; it lies over the grid's first item and moves with its scroll, read while placing
        MapWindow(
            Modifier.fillMaxWidth()
                .offset { IntOffset(0, -mapScrollOf(gridState, (mapHeight + theme.unit * 2).roundToPx())) }
                .padding(theme.unit)
                .height(mapHeight)
                .borderRadius(2, moonShadow = true, outline = true)
        )
    }
}

/** How far the grid has scrolled the map's place up: its scroll, or the place's whole [height] once it has left. */
private fun mapScrollOf(gridState: LazyGridState, height: Int): Int = when (gridState.firstVisibleItemIndex) {
    0 -> gridState.firstVisibleItemScrollOffset
    else -> height
}

/** The narrowest column of the feed's grid: the web's `minmax(300px, 1fr)`. */
private val GridColumnWidth = 300.dp

/** The height of the map in units, as on the web's home page. */
private const val MapHeightUnits = 48

