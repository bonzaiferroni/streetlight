package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Places [content] in a column, its children [gap] units apart. The web's `Column` with `Gap(n)`. */
@Composable
inline fun Column(
    gap: Number,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) = androidx.compose.foundation.layout.Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(currentTheme.unit * gap.toFloat()),
    horizontalAlignment = horizontalAlignment,
    content = content,
)

/** Places [content] in a row, its children [gap] units apart. The web's `Row` with `Gap(n)`. */
@Composable
inline fun Row(
    gap: Number,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) = androidx.compose.foundation.layout.Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(currentTheme.unit * gap.toFloat()),
    verticalAlignment = verticalAlignment,
    content = content,
)
