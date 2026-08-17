package koala.dom

import kampfire.model.Labeled
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.MutableTap
import koala.model.mutableTapOf
import kotlinx.html.SELECT
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun ViewScope.dropMenu(
    options: List<String>,
    field: MutableTap<String>,
    mod: ModifierSet? = null,
    block: (SELECT.() -> Unit)? = null
): HTMLSelectElement {
    var currentValue = field.now
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
        block?.invoke(this)
    }

    element.addEventListener("change", { event ->
        val newValue = (event.target as HTMLSelectElement).value
        if (newValue != currentValue) {
            field.set(newValue)
            display(field.now)
        }
    })

    launchEffect("dropMenu") {
        field.flow.collect {
            display(it)
        }
    }

    return element
}

inline fun <reified E> ViewScope.dropMenu(
    field: MutableTap<E>,
    mod: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val values = enums.map { it.label }
    val textField = field.mutableTapOf({ it.label }) { enums[values.indexOf(it)] }

    return dropMenu(values, textField, mod, block)
}

inline fun <reified E> ViewScope.dropMenuNullable(
    field: MutableTap<E?>,
    mod: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val values = listOf("None") + enums.map { it.label }
    val textField = field.mutableTapOf({ it?.label ?: "None" }) { value ->
        if (value == "None") null else enums[values.indexOf(value) - 1]
    }

    return dropMenu(values, textField, mod, block)
}
