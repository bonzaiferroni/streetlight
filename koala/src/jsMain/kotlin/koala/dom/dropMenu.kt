package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.SELECT
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun RenderScope.dropMenu(
    options: List<String>,
    flow: Flow<String>? = null,
    onChangeValue: ((String) -> Unit)? = null,
    modifiers: ModifierSet? = null,
    block: (SELECT.() -> Unit)? = null
) {
    val element = select {
        addModifiers(modifiers)
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
}

inline fun <reified E> RenderScope.dropMenu(
    noinline onChangeValue: ((E) -> Unit),
    crossinline provideLabel: (E) -> String,
    flow: Flow<E>? = null,
    modifiers: ModifierSet? = null,
    noinline block: (SELECT.() -> Unit)? = null
) where E : Enum<E> {
    val enums = enumValues<E>()
    val values = enums.map { provideLabel(it) }
    val flow = flow?.map(provideLabel)
    val callback: ((String) -> Unit) = { str ->
        val index = values.indexOf(str)
        onChangeValue(enums[index])
    }

    dropMenu(values, flow, callback, modifiers, block)
}

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