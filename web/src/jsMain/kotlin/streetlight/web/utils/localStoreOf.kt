package streetlight.web.utils

import koala.dom.ViewScope
import koala.model.Store
import koala.model.storeOf
import kotlinx.serialization.json.Json
import web.storage.localStorage

inline fun <reified T> ViewScope.localStoreOf(key: String, initial: T): Store<T> {
    val storedValue: T? = localStorage.getItem(key)?.let {
        Json.decodeFromString(it)
    }
    val store = storeOf(storedValue ?: initial)
    launchEffect {
        store.flow.collect {
            localStorage.setItem(key, Json.encodeToString(it))
        }
    }
    return store
}