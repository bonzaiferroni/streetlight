package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Columns side by side, centered, each a [rowColumn] sharing the width, wrapping onto more rows where two do not fit,
 * two units apart. The basis of a form's layout. The cousin of the web's `BodyStyle.ColumnRow`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnRow(modifier: Modifier = Modifier, content: @Composable FlowRowScope.() -> Unit) {
    val theme = currentTheme
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit * 2, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(theme.unit * 2),
        content = content,
    )
}

/**
 * Sizes this as a column of a [ColumnRow]: at least [ColumnMinWidth] before the row wraps, never wider than
 * [ColumnMaxWidth], and sharing what is left, as the web's `flex: 1 1 400px` held to `540px`.
 */
@OptIn(ExperimentalLayoutApi::class)
context(scope: FlowRowScope)
fun Modifier.rowColumn() = with(scope) { widthIn(min = ColumnMinWidth, max = ColumnMaxWidth).weight(1f) }

/** The width a column of a [ColumnRow] keeps before the row wraps, the web's `400px`. */
val ColumnMinWidth = 400.dp

/** The widest a column of a [ColumnRow] runs, the web's `540px`. */
val ColumnMaxWidth = 540.dp
