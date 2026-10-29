package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A view that scrolls as one lazy grid of [columns], its items a line apart, filled by [content] with sections such as
 * [feedSection].
 */
@Composable
fun LazyGridView(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    columns: GridCells = GridCells.Adaptive(GridColumnWidth),
    content: LazyGridScope.() -> Unit,
) {
    val theme = currentTheme
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.fillMaxSize(),
        state = state,
        verticalArrangement = Arrangement.spacedBy(theme.line),
        horizontalArrangement = Arrangement.spacedBy(theme.line),
        content = content,
    )
}

/** Adds an item across every column of the grid. */
fun LazyGridScope.fullItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key, span = { GridItemSpan(maxLineSpan) }, contentType, content)

/** Adds an item across every column that stays composed when it scrolls away, for content costly to start anew. */
fun LazyGridScope.pinnedItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit,
) = fullItem(key, contentType) {
    val pinnable = LocalPinnableContainer.current
    DisposableEffect(pinnable) {
        val handle = pinnable?.pin()
        onDispose { handle?.release() }
    }
    content()
}

/**
 * A view that scrolls by [state] as one lazy staggered grid of [columns], each column stacking its items whatever their
 * heights, filled by [content] with sections such as [feedSection]. In the `Grid` [mode] its items stand a unit apart
 * and a unit from its edges, so the rounded entries stand clear; otherwise a line apart, each full item padded by the
 * mode's [sideUnits]. It keeps room at its bottom for the dock, and at its top for the top bar while [isUnderTopBar].
 * The masonry cousin of [LazyGridView].
 */
@Composable
fun LazyStaggeredGridView(
    state: LazyStaggeredGridState,
    mode: FeedMode,
    modifier: Modifier = Modifier,
    columns: StaggeredGridCells = StaggeredGridCells.Adaptive(GridColumnWidth),
    isUnderTopBar: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    val theme = currentTheme
    val isGrid = mode == FeedMode.Grid
    val spacing = if (isGrid) theme.unit else theme.line
    val edge = if (isGrid) theme.unit else 0.dp
    val insets = currentPageInsets
    val top = if (isUnderTopBar) insets.calculateTopPadding() else 0.dp
    val bottom = insets.calculateBottomPadding()
    val contentPadding = PaddingValues(start = edge, top = top + edge, end = edge, bottom = bottom + edge)
    LazyVerticalStaggeredGrid(
        columns = columns,
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        verticalItemSpacing = spacing,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/** Adds an item across every column of the staggered grid. */
fun LazyStaggeredGridScope.fullItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) = item(key, contentType, StaggeredGridItemSpan.FullLine, content)

/** Adds an item across every column that stays composed when it scrolls away, for content costly to start anew. */
fun LazyStaggeredGridScope.pinnedItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyStaggeredGridItemScope.() -> Unit,
) = fullItem(key, contentType) {
    val pinnable = LocalPinnableContainer.current
    DisposableEffect(pinnable) {
        val handle = pinnable?.pin()
        onDispose { handle?.release() }
    }
    content()
}

/** The narrowest column of a grid: the web's `minmax(300px, 1fr)`. */
private val GridColumnWidth = 300.dp
