package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.model.data.Galaxy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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

/**
 * The view of [streetlight.compose.model.HomeRoute]: the header, the map, the menus, the galaxies beside the connect picker, the home feed's
 * [feedSection] in masonry, and the footer. The cousin of the web's `homeShell`.
 */
context(model: HomeModel)
@Composable
fun HomeView() {
    val feed by model.feedState.collectAsState()
    val galaxies by model.galaxiesState.collectAsState()
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
        fullItem { AppHeader() }
        pinnedItem {
            MapWindow(
                Modifier.fillMaxWidth()
                    .padding(if (isGrid) 0 else 1)
                    .height(theme.unit * MapHeightUnits)
                    .borderRadius(2, moonShadow = true, outline = Outline.Mid)
            )
        }
        fullItem { HomeMenus(Modifier.padding(horizontal = if (isGrid) 0 else 1)) }
        fullItem { LeadingSection(galaxies, Modifier.padding(horizontal = if (isGrid) 0 else 1, vertical = 4)) }
        feed?.let { feedSection(it, model.feedTypeState, mode, settings.feedModeState) }
        fullItem { AppFooter(HomeViewSource, Modifier.padding(top = 8)) }
    }
}

/**
 * The galaxy menu at the start, and the buttons to create a galaxy and a post at the end, which do nothing until the
 * app has their menus and routes. The cousin of the web's `galaxyMenu`, its Create Galaxy button and `createPostMenu`.
 */
@Composable
private fun HomeMenus(modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button("galaxies", onClick = {}, style = ButtonStyle.Zen)
        Row(1) {
            Button("Create Galaxy", onClick = {}, style = ButtonStyle.Zen)
            Button("Create Post", onClick = {}, style = ButtonStyle.Zen)
        }
    }
}

/**
 * The [galaxies] beside the connect picker, each under its noted heading, side by side from the web's 600px container
 * width and stacked below it. The cousin of the web's `leadingSection`.
 */
@Composable
private fun LeadingSection(galaxies: List<Galaxy>, modifier: Modifier) {
    val theme = currentTheme
    BoxWithConstraints(modifier) {
        val galaxyColumn = @Composable { columnModifier: Modifier ->
            Column(1, columnModifier) {
                NotedHeading("Galaxies", "Communities with a particular focus.")
                galaxies.forEach { SmallGalaxyCard(it) }
            }
        }
        val connectColumn = @Composable { columnModifier: Modifier ->
            Column(1, columnModifier, horizontalAlignment = Alignment.CenterHorizontally) {
                NotedHeading("Get Involved", "Connect to your community through Streetlight.")
                ConnectPicker(Modifier.widthIn(max = theme.unit * PickerMaxUnits).fillMaxWidth())
            }
        }
        if (maxWidth >= SideBySideWidth) {
            Row(1) {
                galaxyColumn(Modifier.weight(1f))
                connectColumn(Modifier.weight(1f))
            }
        } else {
            Column(4) {
                galaxyColumn(Modifier)
                connectColumn(Modifier)
            }
        }
    }
}

/** A [heading] in a filigree with its [note] close below it. The cousin of the web's `notedHeading`. */
@Composable
private fun NotedHeading(heading: String, note: String) {
    val theme = currentTheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            text = heading,
            modifier = Modifier.fillMaxWidth().filigree().opacityHigh(),
            style = theme.text.heading4.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
        BasicText(
            text = note,
            modifier = Modifier.opacityHigh(),
            style = theme.text.small.copy(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
    }
}

/** The width from which the galaxies and the connect picker sit side by side: the web's `ContainerMdRow`. */
private val SideBySideWidth = 600.dp

/** The widest the connect picker grows in units, the web's `MaxWidth(40)`. */
private const val PickerMaxUnits = 40

/** The path of this view's source, linked from its footer. */
private const val HomeViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/HomeView.kt"

/** The height of the map in units, as on the web's home page. */
private const val MapHeightUnits = 48
