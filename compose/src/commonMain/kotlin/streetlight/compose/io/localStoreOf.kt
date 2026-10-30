package streetlight.compose.io

import kampfire.model.Store
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * A store starting from the value last saved in the app-data file [name], or [initial], that saves each change there
 * in [scope] once it settles. The cousin of the web's `localStoreOf`.
 */
@OptIn(FlowPreview::class)
inline fun <reified T> localStoreOf(scope: CoroutineScope, name: String, initial: T): Store<T> {
    val saved = readAppFileNow(name)?.let { runCatching { localStoreJson.decodeFromString<T>(it) }.getOrNull() }
    val store = storeOf(saved ?: initial)
    scope.launch {
        store.flow.drop(1).debounce(SaveDelayMillis).collect { writeAppFile(name, localStoreJson.encodeToString(it)) }
    }
    return store
}

/** The json of an [localStoreOf], which reads a file saved before a field was added or removed. */
@PublishedApi
internal val localStoreJson = Json { ignoreUnknownKeys = true }

/** The time a change holds before it is saved, so a burst of changes writes once. */
@PublishedApi
internal const val SaveDelayMillis = 500L
