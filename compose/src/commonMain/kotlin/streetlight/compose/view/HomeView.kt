package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import streetlight.compose.model.AppSettings
import streetlight.compose.model.HomeModel
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

    val listState = rememberLazyListState()
    val mapHeight = theme.unit * MapHeightUnits

    Box(Modifier.fillMaxSize()) {
        // the parts stand a line apart on their quiet background, so the theme behind draws the line between them
        LazyColumn(Modifier.fillMaxSize(), state = listState, verticalArrangement = Arrangement.spacedBy(theme.line)) {
            item { Spacer(Modifier.fillMaxWidth().padding(theme.unit).height(mapHeight)) }
            feed?.let { feedSection(it, model.feedTypeState, settings.feedModeState) }
        }

        // the map stays out of the list, which would dispose it when it scrolls away and start its renderer anew when
        // it returns; it lies over the list's first item and moves with its scroll, read while placing
        MapWindow(
            Modifier.fillMaxWidth()
                .offset { IntOffset(0, -mapScrollOf(listState, (mapHeight + theme.unit * 2).roundToPx())) }
                .padding(theme.unit)
                .height(mapHeight)
        )
    }
}

/** How far the list has scrolled the map's place up: its scroll, or the place's whole [height] once it has left. */
private fun mapScrollOf(listState: LazyListState, height: Int): Int = when (listState.firstVisibleItemIndex) {
    0 -> listState.firstVisibleItemScrollOffset
    else -> height
}

/** The height of the map in units, as on the web's home page. */
private const val MapHeightUnits = 48

