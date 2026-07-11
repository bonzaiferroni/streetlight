package koala.dom

import kampfire.model.Labeled
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.SELECT
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun AppScope.dropMenu(
    options: List<String>,
    flow: Flow<String>? = null,
    onChangeValue: ((String) -> Unit)? = null,
    mod: ModifierSet? = null,
    block: (SELECT.() -> Unit)? = null
): HTMLSelectElement {
    val element = select {
        addModifiers(mod)
        options.forEach {
            option {
                value = it
                +it
            }
        }
        block?.invoke(this)
    }

    onChangeValue?.let {
        element.addEventListener("change", { event ->
            val target = event.target as HTMLSelectElement
            onChangeValue(target.value)
        })
    }

    flow?.let {
        parentScope.launch {
            flow.distinctUntilChanged().collect {
                element.value = it
            }
        }
    }

    return element
}

inline fun <reified E: Enum<E>> AppScope.dropMenu(
    noinline onChangeValue: ((E) -> Unit),
    crossinline provideLabel: (E) -> String,
    flow: Flow<E>? = null,
    mod: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
): HTMLSelectElement {
    val enums = enumValues<E>()
    val values = enums.map { provideLabel(it) }
    val flow = flow?.map(provideLabel)
    val callback: ((String) -> Unit) = { str ->
        val index = values.indexOf(str)
        onChangeValue(enums[index])
    }

    return dropMenu(values, flow, callback, mod, block)
}

inline fun <reified E> AppScope.dropMenu(
    store: Store<E>,
    modifiers: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
) where E: Enum<E>, E: Labeled = dropMenu(store::set, { it.label }, store.flow, modifiers, block)

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