package koala.dom

import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.html.applyBlockLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.SELECT
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun RenderContext.dropMenu(
    options: List<String>,
    flow: Flow<String>? = null,
    onChangeValue: ((String) -> Unit)? = null,
    modifiers: ModifierSet? = null,
    block: (SELECT.() -> Unit)? = null
) {
    val element = select {
        applyModifiers(modifiers)
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
        renderScope.launch {
            flow.distinctUntilChanged().collect {
                element.value = it
            }
        }
    }
}

inline fun <reified T> RenderContext.dropMenu(
    noinline onChangeValue: ((T) -> Unit)? = null,
    flow: Flow<T>? = null,
    modifiers: ModifierSet? = null,
    crossinline provideLabel: (T) -> String,
    noinline block: (SELECT.() -> Unit)? = null
) where T : Enum<T> {
    val enums = enumValues<T>()
    val values = enums.map { provideLabel(it) }
    val flow = flow?.map(provideLabel)
    val callback: ((String) -> Unit)? = onChangeValue?.let {
        { str ->
            val index = values.indexOf(str)
            onChangeValue(enums[index])
        }
    }

    dropMenu(values, flow, callback, modifiers, block)
}

// inline fun <reified T> DropMenu(
//    selected: T,
//    crossinline labelOf: (T) -> String,
//    modifier: Modifier = Modifier,
//    color: Color = Pond.colors.primary,
//    label: String? = null,
//    crossinline onChange: (T) -> Unit
//) where T : Enum<T> {