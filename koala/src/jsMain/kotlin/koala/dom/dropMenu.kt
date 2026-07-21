package koala.dom

import kampfire.model.Labeled
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.MutableField
import koala.model.Store
import koala.model.mutableFieldOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.SELECT
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun ViewScope.dropMenu(
    options: List<String>,
    field: MutableField<String>,
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

inline fun <reified E: Enum<E>> ViewScope.dropMenu(
    field: MutableField<E>,
    crossinline provideLabel: (E) -> String,
    mod: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement {
    val enums = enumValues<E>()
    val values = enums.map { provideLabel(it) }
    val textField = field.mutableFieldOf({ provideLabel(it) }) { enums[values.indexOf(it)] }

    return dropMenu(values, textField, mod, block)
}

inline fun <reified E> ViewScope.dropMenu(
    store: Store<E>,
    modifiers: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
) where E: Enum<E>, E: Labeled = dropMenu(store, { it.label }, modifiers, block)

//inline fun <reified E, Data> WireContext<Data>.dropMenu(
//    noinline write: (E) -> Data,
//    crossinline provideLabel: (E) -> String,
//) {
//    dropMenu(
//        onChangeValue = { value -> state.set { write(value) } },
//        provideLabel = provideLabel,
//        flow =
//    )
//}

// inline fun <reified T> DropMenu(
//    selected: T,
//    crossinline labelOf: (T) -> String,
//    modifier: Modifier = Modifier,
//    color: Color = Pond.colors.primary,
//    label: String? = null,
//    crossinline onChange: (T) -> Unit
//) where T : Enum<T> {