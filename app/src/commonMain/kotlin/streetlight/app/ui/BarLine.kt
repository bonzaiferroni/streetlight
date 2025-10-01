package streetlight.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pondui.ui.theme.Pond

@Composable
fun BarLine(modifier: Modifier = Modifier) {
    Box(
        modifier.width(1.dp)
            .fillMaxHeight()
            .background(Pond.localColors.contentDim)
    )
}