package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** A bar filled to [progress], from 0 to 1. */
fun FlowContent.progressBar(
    progress: Float = 0f,
    mod: Modifier? = modify(Height(2), MinWidth(16)),
    barMod: Modifier? = PrimaryBg,
    config: DIV.() -> Unit = { },
) {
    div(mod) {
        config()
        setStyle(ProgressBarStyle.Progress.of(progress.coerceIn(0f, 1f)))
        div(modify(barMod, ProgressBarStyle.Indicator))
    }
}

object ProgressBarStyle {
    val Container = Class("progress-bar")
    val Indicator = Container.withBemElement("indicator")

    val Progress = Property<Number>("bar-progress", true)
}

// language="CSS"
val ProgressBarCss get() = with(ProgressBarStyle) { """
$Indicator {
    width: calc(var($Progress) * 100%);
    height: 100%;
    transition: var(--transition-width);
}
""" }