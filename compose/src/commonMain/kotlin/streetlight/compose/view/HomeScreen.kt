package streetlight.compose.view

import streetlight.model.ui.TopBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import streetlight.compose.model.HomeModel
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.compose.model.currentPortal
import streetlight.compose.model.GalaxyRoute
import streetlight.compose.model.EarthRoute
import streetlight.compose.modifier.pointerClick
import streetlight.compose.model.HomeRoute
import streetlight.model.data.HomeContent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The screen of [HomeRoute]: the logo as its top bar, and its content once inflated, shown in the [HomeView]. */
@Composable
fun HomeScreen() {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, gridState)
    Inflated<HomeContent>(HomeRoute) { content ->
        context(koinViewModel<HomeModel> { parametersOf(content) }) { HomeView(gridState) }
    }
}

/**
 * The view of [HomeRoute], in a grid scrolled by [gridState] under the top bar: the map, the menus, the galaxies beside
 * the connect picker, the home feed's [feedSection] in masonry, and the footer. The cousin of the web's `homeShell`.
 */
@Composable
context(model: HomeModel)
fun HomeView(gridState: LazyStaggeredGridState) {
    val page by model.feed.feedState.collectAsState()
    val theme = currentTheme
    PageThemeEffect(null)

    val mode by model.feed.modeState.collectAsState()

    LazyStaggeredGridView(gridState, mode) {
        fullItem {
            MapMount(
                Modifier.fillMaxWidth()
                    .padding(mode.sideUnits)
                    .height(theme.unit * MapHeightUnits)
                    .borderRadius(2, moonShadow = true),
                markerMap = model.markerMap,
            )
        }
        fullItem { HomeMenus(Modifier.padding(horizontal = mode.sideUnits)) }
        fullItem { LeadingSection(Modifier.padding(horizontal = mode.sideUnits, vertical = 4)) }
        context(model.feed) { feedSection(page, mode) }
        fullItem { AppFooter(HomeViewSource, Modifier.padding(top = 8)) }
    }
}

/**
 * The galaxy menu and a button to the earth at the start, and the button to create a galaxy and the menu to create a
 * post at the end; the galaxy's two do nothing until the app has their menu and route. The cousin of the web's
 * `galaxyMenu`, its Create Galaxy button and `createPostMenu`.
 */
@Composable
private fun HomeMenus(modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val portal = currentPortal
        Row(1) {
            Button("galaxies", onClick = {}, style = ButtonStyle.Zen)
            // a passage to the earth until the app has its route dock
            Button("earth", onClick = { portal.go(EarthRoute) }, style = ButtonStyle.Zen)
        }
        Row(1) {
            Button("Create Galaxy", onClick = {}, style = ButtonStyle.Zen)
            CreatePostMenu(null)
        }
    }
}

/**
 * The [model]'s top galaxies beside the connect picker, each under its noted heading, side by side from the web's 600px
 * container width and stacked below it. The cousin of the web's `leadingSection`.
 */
@Composable
context(model: HomeModel)
private fun LeadingSection(modifier: Modifier) {
    val galaxies = model.content.galaxies
    val theme = currentTheme
    val portal = currentPortal
    BoxWithConstraints(modifier) {
        val galaxyColumn = @Composable { columnModifier: Modifier ->
            Column(1, columnModifier) {
                NotedHeading("Galaxies", "Communities with a particular focus.")
                galaxies.forEach { galaxy ->
                    SmallGalaxyCard(galaxy, Modifier.pointerClick { portal.go(GalaxyRoute(galaxy.slug)) })
                }
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
private fun NotedHeading(heading: String, note: String, modifier: Modifier = Modifier) {
    val theme = currentTheme
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
private const val HomeViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/HomeScreen.kt"

/** The height of the map in units, as on the web's home page. */
private const val MapHeightUnits = 48
