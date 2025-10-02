package streetlight.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pondui.ui.theme.Pond

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowScope.BarLine(
    isBold: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.width(if (isBold) 2.dp else 1.dp)
            .fillMaxRowHeight()
            .background(Pond.localColors.contentDim)
    )
}