package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.MutableTap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.input
import org.w3c.dom.HTMLInputElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun ViewScope.datetimeInput() {
    input {
        type = InputType.dateTimeLocal
    }
}

fun ViewScope.dateInput(
    field: MutableTap<LocalDate?>,
    modifiers: ModifierSet? = null,
    block: INPUT.() -> Unit = {},
): HTMLInputElement {
    var currentValue = field.now
    lateinit var element: HTMLInputElement

    fun display(value: LocalDate?) {
        currentValue = value
        val text = value?.toString() ?: ""
        if (element.value != text) {
            element.value = text
        }
    }

    element = input {
        addModifiers(modifiers)
        type = InputType.date
        value = currentValue?.toString() ?: ""
        block()
    }

    element.addEventListener("input", {
        val text = element.value
        val newValue = if (text.isBlank()) null else LocalDate.parse(text)
        if (newValue != currentValue) {
            field.set(newValue)
            display(field.now)
        }
    })

    launchEffect("dateInput") {
        field.flow.collect {
            display(it)
        }
    }

    return element
}

fun ViewScope.timeInput(
    field: MutableTap<LocalTime?>,
    modifiers: ModifierSet? = null,
    step: Duration = 5.minutes,
    block: INPUT.() -> Unit = {}
): HTMLInputElement {
    var currentValue = field.now
    lateinit var element: HTMLInputElement

    fun display(value: LocalTime?) {
        currentValue = value
        val text = value?.toInputValue() ?: ""
        if (element.value != text) {
            element.value = text
        }
    }

    element = input {
        addModifiers(modifiers)
        type = InputType.time
        this.step = step.inWholeSeconds.toString()
        value = currentValue?.toInputValue() ?: ""
        block()
    }

    element.addEventListener("input", {
        val text = element.value
        val newValue = if (text.isBlank()) null else LocalTime.parse(text)
        if (newValue != currentValue) {
            field.set(newValue)
            display(field.now)
        }
    })

    launchEffect("timeInput") {
        field.flow.collect {
            display(it)
        }
    }

    return element
}

private fun LocalTime.toInputValue(): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')