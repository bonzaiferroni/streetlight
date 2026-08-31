package streetlight.web.utils

import koala.dom.ViewScope
import kampfire.model.Store
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import web.storage.localStorage

inline fun <reified T> localStoreOf(scope: CoroutineScope, key: String, initial: T): Store<T> {
    val storedValue: T? = localStorage.getItem(key)?.let {
        Json.decodeFromString(it)
    }
    val store = storeOf(storedValue ?: initial)
    scope.launch {
        store.flow.collect {
            localStorage.setItem(key, Json.encodeToString(it))
        }
    }
    return store
}

inline fun <reified T> ViewScope.localStoreOf(key: String, initial: T) =
    localStoreOf(contentScope, key, initial)