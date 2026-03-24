package koala.dom

import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.html.InputType
import kotlinx.html.js.input

fun RenderContext.datetimeInput() {
    input {
        type = InputType.dateTimeLocal
    }
}

fun RenderContext.dateInput(
    flow: Flow<LocalDate?>,
    onValueChanged: (LocalDate) -> Unit,
    modifiers: ModifierSet? = null,
) {
    val element = input {
        setModifiers(modifiers)
        type = InputType.date
    }

    var last: LocalDate? = null

    element.addEventListener("input", {
        val v = element.value
        if (v.isBlank()) return@addEventListener

        val parsed = LocalDate.parse(v)
        if (parsed != last) {
            last = parsed
            onValueChanged(parsed)
        }
    })

    renderScope.launch {
        flow.collect { d ->
            if (d != last) {
                last = d
                element.value = d?.toString() ?: ""
            }
        }
    }
}

fun RenderContext.timeInput(
    flow: Flow<LocalTime?>,
    onValueChanged: (LocalTime) -> Unit,
    modifiers: ModifierSet? = null,
) {
    val element = input {
        setModifiers(modifiers)
        type = InputType.time
    }

    var last: LocalTime? = null

    element.addEventListener("input", {
        val v = element.value
        if (v.isBlank()) return@addEventListener

        val parsed = LocalTime.parse(v)
        if (parsed != last) {
            last = parsed
            onValueChanged(parsed)
        }
    })

    renderScope.launch {
        flow.collect { t ->
            if (t != last) {
                last = t
                element.value = t?.toInputValue() ?: ""
            }
        }
    }
}

private fun LocalTime.toInputValue(): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')