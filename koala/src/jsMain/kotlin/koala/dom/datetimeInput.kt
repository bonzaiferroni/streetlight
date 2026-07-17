package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun ViewScope.datetimeInput() {
    input {
        type = InputType.dateTimeLocal
    }
}

fun ViewScope.dateInput(
    flow: Flow<LocalDate?>,
    onValueChanged: (LocalDate) -> Unit,
    modifiers: ModifierSet? = null,
    block: INPUT.() -> Unit = {},
) {
    val element = input {
        addModifiers(modifiers)
        type = InputType.date
        block()
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

    parentScope.launch {
        flow.collect { d ->
            if (d != last) {
                last = d
                element.value = d?.toString() ?: ""
            }
        }
    }
}

fun ViewScope.timeInput(
    flow: Flow<LocalTime?>,
    onValueChanged: (LocalTime) -> Unit,
    modifiers: ModifierSet? = null,
    step: Duration = 5.minutes,
    block: INPUT.() -> Unit = {}
) {
    val element = input {
        addModifiers(modifiers)
        type = InputType.time
        this.step = step.inWholeSeconds.toString()
        block()
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

    parentScope.launch {
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