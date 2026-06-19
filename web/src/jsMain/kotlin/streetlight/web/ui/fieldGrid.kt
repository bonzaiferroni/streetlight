package streetlight.web.ui

import kampfire.model.GeoPoint
import koala.css.*
import koala.dom.*
import koala.html.box
import koala.html.em
import koala.html.span
import koala.html.strong
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun TagScope.fieldGrid(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) {
    grid(columnsOf(LinearDimension.auto, 1.fr, LinearDimension.auto), mod = mod) {
        content()
    }
}

fun TagScope.fieldValue(
    value: String?,
    previousValue: String?,
    label: String
) {
    strong(label)
    val swap = swap {
        valueCell(value)
        valueCell(previousValue)
    }
    button(onClick = swap::next) {
        +"swap"
    }
}

fun TagScope.valueCell(value: String?) {
    when (value) {
        null -> em("none")
        else -> span(value)
    }
}

fun TagScope.fieldValue(
    value: GeoPoint?,
    previousValue: GeoPoint?,
    label: String = "geolocation"
) {
    fieldValue(value?.toString(), previousValue?.toString(), label)
}