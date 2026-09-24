package streetlight.web.utils

import koala.dom.ViewScope
import kampfire.model.Store
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import web.storage.localStorage

/** Reads stored state that has changed shape: unknown keys are skipped and unknown enum values take their default. */
@PublishedApi
internal val localStoreJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/** A store kept in local storage as JSON under [key], starting at [initial] when nothing is stored. */
inline fun <reified T> localStoreOf(scope: CoroutineScope, key: String, initial: T): Store<T> {
    val storedValue: T? = localStorage.getItem(key)?.let {
        localStoreJson.decodeFromString<T>(it)
    }
    val store = storeOf(storedValue ?: initial)
    scope.launch {
        store.flow.collect {
            localStorage.setItem(key, localStoreJson.encodeToString(it))
        }
    }
    return store
}

inline fun <reified T> ViewScope.localStoreOf(key: String, initial: T) =
    localStoreOf(contentScope, key, initial)