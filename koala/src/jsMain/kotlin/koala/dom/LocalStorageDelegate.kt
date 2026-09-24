package koala.dom

import koala.utils.jsonConfig
import kotlinx.browser.localStorage
import org.w3c.dom.get
import kotlin.reflect.KProperty

/** A property backed by `localStorage` under [key], reading [defaultValue] when nothing is stored. */
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

/** A [LocalStorageDelegate] converting its value with [getter] and [setter]. */
fun <T> storageOf(
    key: String,
    getter: (String) -> T,
    setter: (T) -> String,
    defaultValue: T,
) = LocalStorageDelegate(key, getter, setter, defaultValue)

/** A [LocalStorageDelegate] storing its value as JSON. */
inline fun <reified T> jsonStorageOf(
    key: String,
    defaultValue: T,
) = storageOf(
    key = key,
    getter = { jsonConfig.decodeFromString<T>(it) },
    setter = { jsonConfig.encodeToString(it) },
    defaultValue = defaultValue,
)

/** A [LocalStorageDelegate] storing a list as JSON, empty by default. */
inline fun <reified T> jsonListStorageOf(
    key: String,
) = storageOf(
    key = key,
    getter = { jsonConfig.decodeFromString<List<T>>(it) },
    setter = { jsonConfig.encodeToString(it) },
    defaultValue = emptyList(),
)

/** A [LocalStorageDelegate] storing a set as comma-separated values, empty by default. */
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