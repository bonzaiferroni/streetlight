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
 * A view that scrolls as one lazy staggered grid of [columns], each column stacking its items [spacing] apart whatever
 * their heights and held [contentPadding] from its edges, filled by [content] with sections such as [feedSection]. The
 * masonry cousin of [LazyGridView].
 */
@Composable
fun LazyStaggeredGridView(
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    columns: StaggeredGridCells = StaggeredGridCells.Adaptive(GridColumnWidth),
    spacing: Dp = currentTheme.line,
    contentPadding: PaddingValues = PaddingValues(),
    content: LazyStaggeredGridScope.() -> Unit,
) {
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
