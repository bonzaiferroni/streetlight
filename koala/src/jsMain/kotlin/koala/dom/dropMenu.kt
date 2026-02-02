package koala.dom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.js.option
import kotlinx.html.js.select
import org.w3c.dom.HTMLSelectElement

fun RenderContext.dropMenu(
    options: List<String>,
    flow: Flow<String>? = null,
    onChangeValue: ((String) -> Unit)? = null
) {
    val element = select {
        options.forEach {
            option {
                value = it
                +it
            }
        }
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
    crossinline labelOf: (T) -> String,
) where T : Enum<T> {
    val enums = enumValues<T>()
    val values = enums.map { labelOf(it) }
    val flow = flow?.map(labelOf)
    val callback: ((String) -> Unit)? = onChangeValue?.let {
        { str ->
            val index = values.indexOf(str)
            onChangeValue(enums[index])
        }
    }

    dropMenu(values, flow, callback)
}

// inline fun <reified T> DropMenu(
//    selected: T,
//    crossinline labelOf: (T) -> String,
//    modifier: Modifier = Modifier,
//    color: Color = Pond.colors.primary,
//    label: String? = null,
//    crossinline onChange: (T) -> Unit
//) where T : Enum<T> {