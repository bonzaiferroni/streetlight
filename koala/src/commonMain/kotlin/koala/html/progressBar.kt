package koala.html

import koala.css.*
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.progressBar(
    progress: Float = 0f,
    mod: ModifierSet? = modify(Height2, MinWidth16),
    barMod: ModifierSet? = modify(PrimaryBg),
    config: DIV.() -> Unit = { },
) {
    div(mod) {
        config()
        setStyle(ProgressBarStyle.Progress.to(progress.coerceIn(0f, 1f)))
        div(modify(barMod, ProgressBarStyle.Indicator))
    }
}

object ProgressBarStyle {
    val Container = Class("progress-bar")
    val Indicator = Container.withBemElement("indicator")

    val Progress = Property<Number>("bar-progress")
}

// language="CSS"
val ProgressBarCss get() = with(ProgressBarStyle) { """
$Indicator {
    width: calc(var($Progress) * 100%);
    height: 100%;
    transition: width var(--magic-interval) var(--magic-easing);
}
""" }