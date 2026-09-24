package koala.dom

import kampfire.model.Labeled
import koala.modifier.*
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kotlinx.html.SELECT
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

/** A select of [options] bound to [state]. */
fun ViewScope.dropMenu(
    options: List<String>,
    state: MutableTap<String>,
    mod: Modifier? = null,
    config: (SELECT.() -> Unit)? = null
): HTMLSelectElement {
    var currentValue = state.now
    lateinit var element: HTMLSelectElement

    fun display(value: String) {
        currentValue = value
        if (element.value != value) {
            element.value = value
        }
    }

    element = select {
        addModifiers(mod)
        options.forEach {
            option {
                value = it
                selected = it == currentValue
                +it
            }
        }
        config?.invoke(this)
    }

    element.addEventListener("change", { event ->
        val newValue = (event.target as HTMLSelectElement).value
        if (newValue != currentValue) {
            state.set(newValue)
            display(state.now)
        }
    })

    launchEffect("dropMenu") {
        state.flow.collect {
            display(it)
        }
    }

    return element
}

/** A select of [trueLabel] and [falseLabel] bound to the boolean [state]. */
fun ViewScope.dropMenu(
    trueLabel: String,
    falseLabel: String,
    state: MutableTap<Boolean>,
    mod: Modifier? = null,
    config: (SELECT.() -> Unit)? = null,
) = dropMenu(
    options = listOf(trueLabel, falseLabel),
    state = state.mutableTapOf({ if (it) trueLabel else falseLabel }) { it == trueLabel },
    mod = mod,
    config = config
)

/** A select of every entry of the enum [E], by label, bound to [state]. */
inline fun <reified E> ViewScope.dropMenu(
    state: MutableTap<E>,
    mod: Modifier? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val values = enums.map { it.label }
    val textField = state.mutableTapOf({ it.label }) { enums[values.indexOf(it)] }

    return dropMenu(values, textField, mod, block)
}

/** A select of "None" and every entry of the enum [E], by label, bound to [field]. */
inline fun <reified E> ViewScope.dropMenuNullable(
    field: MutableTap<E?>,
    mod: Modifier? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val values = listOf("None") + enums.map { it.label }
    val textField = field.mutableTapOf({ it?.label ?: "None" }) { value ->
        if (value == "None") null else enums[values.indexOf(value) - 1]
    }

    return dropMenu(values, textField, mod, block)
}
