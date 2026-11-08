package streetlight.web.utility

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import web.events.EventHandler
import web.idb.IDBDatabase
import web.idb.IDBObjectStore
import web.idb.IDBRequest
import web.idb.IDBRequestReadyState
import web.idb.IDBTransactionMode
import web.idb.IDBValidKey
import web.idb.done
import web.idb.indexedDB
import web.idb.readonly
import web.idb.readwrite
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * The browser database [name], holding an object store for each of [storeNames]. Opening it at a higher [version]
 * creates the stores it lacks, so adding a store means raising the version.
 */
class IdbDatabase(
    private val name: String,
    private val version: Int,
    private val storeNames: List<String>,
) {
    private var opened: CompletableDeferred<IDBDatabase>? = null

    /** The open database, opened on the first call. */
    suspend fun read(): IDBDatabase {
        opened?.let { return it.await() }
        val deferred = CompletableDeferred<IDBDatabase>().also { opened = it }
        try {
            val request = indexedDB.open(name, version.toDouble())
            request.onupgradeneeded = EventHandler {
                val db = request.result
                storeNames.filterNot { db.objectStoreNames.contains(it) }.forEach { db.createObjectStore(it) }
            }
            deferred.complete(request.await().unsafeCast<IDBDatabase>())
        } catch (error: Throwable) {
            opened = null
            deferred.completeExceptionally(error)
        }
        return deferred.await()
    }
}

/** The object store [storeName] of [db], holding each [T] as JSON under the key [keyOf] gives it. */
class IdbStore<T>(
    private val db: IdbDatabase,
    private val storeName: String,
    private val serializer: KSerializer<T>,
    private val keyOf: (T) -> String,
) {
    /** The value under [key], or `null` when there is none. */
    suspend fun read(key: String): T? {
        val text = readStore(IDBTransactionMode.readonly).get(IDBValidKey(key)).await() ?: return null
        return idbJson.decodeFromString(serializer, text.unsafeCast<String>())
    }

    /** Each value of the store. */
    suspend fun readAll(): List<T> = readStore(IDBTransactionMode.readonly).getAll().await()
        .unsafeCast<Array<String>>()
        .map { idbJson.decodeFromString(serializer, it) }

    /** Stores [value] in place of the value under its key. */
    suspend fun write(value: T) {
        val text = idbJson.encodeToString(serializer, value)
        readStore(IDBTransactionMode.readwrite).put(text.toJsString(), IDBValidKey(keyOf(value))).await()
    }

    /** Stores each of [values] in one transaction. */
    suspend fun writeAll(values: List<T>) {
        if (values.isEmpty()) return
        val store = readStore(IDBTransactionMode.readwrite)
        values.map { store.put(idbJson.encodeToString(serializer, it).toJsString(), IDBValidKey(keyOf(it))) }
            .forEach { it.await() }
    }

    /** Removes the value under [key], if there is one. */
    suspend fun remove(key: String) {
        readStore(IDBTransactionMode.readwrite).delete(IDBValidKey(key)).await()
    }

    private suspend fun readStore(mode: IDBTransactionMode): IDBObjectStore =
        db.read().transaction(storeName, mode).objectStore(storeName)
}

/** The result of the request once it succeeds, read at once when it is already done. */
suspend fun IDBRequest<*>.await(): JsAny? {
    if (readyState == IDBRequestReadyState.done) return error?.let { throw Error(it.message) } ?: result
    return suspendCancellableCoroutine { continuation ->
        onsuccess = EventHandler { continuation.resume(result) }
        onerror = EventHandler { continuation.resumeWithException(Error(error?.message ?: "IndexedDB request failed")) }
    }
}

/** Reads values that have changed shape: unknown keys are skipped and unknown enum values take their default. */
private val idbJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
