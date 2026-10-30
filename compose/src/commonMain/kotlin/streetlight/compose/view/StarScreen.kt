package streetlight.compose.view

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.StarModel
import streetlight.compose.model.StarRoute
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.StarContent
import streetlight.model.ui.TopBar

/** The screen of [route]: the logo as its top bar, and its star once inflated, shown in the [StarView]. */
@Composable
fun StarScreen(route: StarRoute) {
    val gridState = rememberLazyStaggeredGridState()
    TopBarMount(TopBar.Logo, gridState)
    Inflated<StarContent>(route) { content ->
        context(koinViewModel<StarModel> { parametersOf(content) }) { StarView(gridState) }
    }
}

/**
 * The view of [StarRoute], in a grid scrolled by [gridState] under the top bar: the star's page layout, in the
 * star's own theme, and the footer. The cousin of the web's `starShell`.
 */
@Composable
context(model: StarModel)
fun StarView(gridState: LazyStaggeredGridState) {
    val page by model.feed.feedState.collectAsState()
    val mode by model.feed.modeState.collectAsState()
    PageThemeEffect(model.content.design?.theme)

    LazyStaggeredGridView(gridState, mode) {
        context(model.feed) { renderLayout(model.content, page, mode, side = mode.sideUnits) }
        fullItem { AppFooter(StarViewSource, Modifier.padding(top = 8)) }
    }
}

/** The path of this view's source, linked from its footer. */
private const val StarViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/StarScreen.kt"
