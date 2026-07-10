package koala.dom

import koala.external.AxisLabelOption
import koala.external.AxisLineOption
import koala.external.AxisOption
import koala.external.AxisPointerLabelOption
import koala.external.AxisPointerOption
import koala.external.EChartsTheme
import koala.external.EmphasisOption
import koala.external.LineStyleOption
import koala.external.SplitLineOption
import koala.external.TextStyleOption
import koala.external.ThemeAxisOption
import koala.external.ThemeLegendOption
import koala.external.ThemeLineOption
import koala.external.ThemeTitleOption
import koala.external.ThemeTooltipOption
import koala.external.TimeLabelFormatter
import kotlinx.browser.document
import kotlinx.browser.window

object ChartUtility {
    val DefaultTimeAxis = AxisOption(
        type = "time",
        axisLabel = AxisLabelOption(formatter = TimeLabelFormatter(day = "{MMM} {d}"))
    )

    val LineColors = listOf(
        rgbVar("--light-1"),
        rgbVar("--light-2"),
        rgbVar("--light-3"),
    )

    fun getLineColor(index: Int) = LineColors[index % LineColors.size]

    fun cssVar(name: String): String =
        window.getComputedStyle(document.documentElement!!)
            .getPropertyValue(name)
            .trim()

    fun rgb(value: String) = "rgb($value)"

    fun rgbVar(cssVar: String) = rgb(cssVar(cssVar))

    fun buildTheme(): EChartsTheme {
        val ink = cssVar("--ink-fg")
        val muted = cssVar("--ink-dim")
        val grid = cssVar("--outline-low-fg")
        val font = cssVar("--font-family")


        val axis = ThemeAxisOption(
            axisLine = AxisLineOption(lineStyle = LineStyleOption(color = muted)),
            splitLine = SplitLineOption(lineStyle = LineStyleOption(color = grid)),
            minorSplitLine = SplitLineOption(lineStyle = LineStyleOption(color = grid))
        )

        return EChartsTheme(
            color = arrayOf(
                rgbVar("--light-1"),
                rgbVar("--light-2"),
                rgbVar("--light-3"),
            ),
            backgroundColor = "transparent",
            textStyle = TextStyleOption(color = ink, fontFamily = font),
            title = ThemeTitleOption(
                textStyle = TextStyleOption(color = ink, fontWeight = "600"),
                subtextStyle = TextStyleOption(color = muted)
            ),
            legend = ThemeLegendOption(textStyle = TextStyleOption(color = ink)),
            axisPointer = AxisPointerOption(
                lineStyle = LineStyleOption(color = muted),
                crossStyle = LineStyleOption(color = muted),
                label = AxisPointerLabelOption(color = ink)
            ),
            timeAxis = axis,
            valueAxis = axis,
            line = ThemeLineOption(
                symbol = "circle",
                emphasis = EmphasisOption(lineStyle = LineStyleOption(width = 3.0))
            ),
            tooltip = ThemeTooltipOption(
                backgroundColor = cssVar("--paper-bg"),
                borderColor = grid,
                borderWidth = 1.0,
                textStyle = TextStyleOption(color = ink, fontFamily = font)
            )
        )
    }
}

