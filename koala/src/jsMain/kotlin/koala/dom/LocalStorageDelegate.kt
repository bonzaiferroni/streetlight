package koala.dom

import koala.utils.jsonConfig
import kotlinx.browser.localStorage
import org.w3c.dom.get
import kotlin.reflect.KProperty

class LocalStorageDelegate<T>(
    private val key: String,
    private val getter: (String) -> T,
    private val setter: (T) -> String,
    private val defaultValue: T,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        localStorage[key]?.let(getter) ?: defaultValue

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        localStorage.setItem(key, setter(value))
    }
}

fun <T> storageOf(
    key: String,
    getter: (String) -> T,
    setter: (T) -> String,
    defaultValue: T,
) = LocalStorageDelegate(key, getter, setter, defaultValue)

inline fun <reified T> jsonStorageOf(
    key: String,
    defaultValue: T,
) = storageOf(
    key = key,
    getter = { jsonConfig.decodeFromString<T>(it) },
    setter = { jsonConfig.encodeToString(it) },
    defaultValue = defaultValue,
)

inline fun <reified T> jsonListStorageOf(
    key: String,
) = storageOf(
    key = key,
    getter = { jsonConfig.decodeFromString<List<T>>(it) },
    setter = { jsonConfig.encodeToString(it) },
    defaultValue = emptyList(),
)

fun <T> setStorageOf(
    key: String,
    toString: (T) -> String = { it.toString() },
    transform: (String) -> T
): LocalStorageDelegate<Set<T>> = storageOf(
    key = key,
    getter = { value -> when (value.isEmpty()) {
        true -> emptySet()
        else -> value.split(',').mapNotNull(transform).toSet()
    } },
    setter = { value -> value.joinToString(",", transform = toString) },
    defaultValue = emptySet()
)