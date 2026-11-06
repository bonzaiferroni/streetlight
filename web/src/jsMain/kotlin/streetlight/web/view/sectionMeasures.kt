package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.div
import koala.modifier.*
import kotlinx.css.pct
import streetlight.web.model.SectionNotation

/** Renders the measures of [notation] as a strip with [mod], each marked by a vertical line at its start. */
fun ViewScope.sectionMeasures(notation: SectionNotation, mod: Modifier? = null) {
    div(modify(mod, MeasureStyle.Strip)) {
        notation.measures.forEach { share ->
            div(modify(MeasureStyle.Measure, Css.Width.of((share * 100).pct)))
        }
    }
}
