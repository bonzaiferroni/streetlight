package koala.dom

import koala.external.AxisLineOption
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
import kotlinx.browser.document
import kotlinx.browser.window

private fun cssVar(name: String): String =
    window.getComputedStyle(document.documentElement!!)
        .getPropertyValue(name)
        .trim()

fun buildTheme(): EChartsTheme {
    val ink = cssVar("--ink-fg")
    val muted = cssVar("--ink-dim")
    val grid = cssVar("--outline-low-fg")
    val font = cssVar("--font-family")
    fun rgb(value: String) = "rgb($value)"

    val axis = ThemeAxisOption(
        axisLine = AxisLineOption(lineStyle = LineStyleOption(color = muted)),
        splitLine = SplitLineOption(lineStyle = LineStyleOption(color = grid)),
        minorSplitLine = SplitLineOption(lineStyle = LineStyleOption(color = grid))
    )

    return EChartsTheme(
        color = arrayOf(
            rgb(cssVar("--light-1")),
            rgb(cssVar("--light-2")),
            rgb(cssVar("--light-3")),
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