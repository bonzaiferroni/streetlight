package streetlight.compose.charts

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

interface ChartScope {
    val config: ChartConfig
    val dimensionX: ChartDimension
    val leftAxis: ChartAxis?
    val rightAxis: ChartAxis?
    val bottomAxis: ChartAxis?
    val chartLeftMarginPx: Float
    val chartRightMarginPx: Float
    val chartTopMarginPx: Float
    val chartBottomMarginPx: Float
    val chartWidthPx: Float
    val chartHeightPx: Float
    val sizePx: Size
    val density: Float
    val axisMarginPx: Float
    val labelHeightPx: Float
    val labelPaddingPx: Float
    val labelFontSize: TextUnit
    val textRuler: TextMeasurer
    val horizontalLineWidthPx: Float

    fun Dp.toPx(): Float = this.value * density
}
