package streetlight.web.idb

import js.array.ReadonlyArray
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import streetlight.web.utility.await
import web.events.EventHandler
import web.idb.IDBDatabase
import web.idb.IDBTransaction
import web.idb.IDBTransactionMode
import web.idb.indexedDB
import web.idb.readonly
import web.idb.readwrite
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * The browser database [name] at [version], holding the tables declared with [table] before it first opens.
 *
 * Opening it at a higher [version] creates the object stores it lacks, so adding a table means raising the version.
 */
class IdbDb(
    private val name: String,
    private val version: Int,
) {
    private val tables = mutableListOf<IdbTable<*>>()
    private val writeMutex = Mutex()
    private var opened: CompletableDeferred<IDBDatabase>? = null

    /** Declares the table [name], holding each [T] under the key [keyOf] gives it. */
    fun <T : Any> table(name: String, serializer: KSerializer<T>, keyOf: (T) -> String): IdbTable<T> {
        requireUnopened(name)
        return IdbTable(this, name, serializer, keyOf).also { tables += it }
    }

    /**
     * Commits the writes [block] makes in one transaction, with the rows removed through foreign keys, then emits the
     * rows of each table it changed. Writes run one at a time, and one that has begun finishes despite cancellation.
     */
    suspend fun write(block: IdbWrite.() -> Unit) = writeMutex.withLock {
        val database = open()
        val write = IdbWrite(tables).apply(block)
        val changes = write.touchedChanges
        if (changes.isEmpty()) return@withLock

        withContext(NonCancellable) {
            val transaction = database.transaction(storeNamesOf(changes.map { it.tableName }), IDBTransactionMode.readwrite)
            changes.forEach { it.request(transaction) }
            transaction.awaitComplete()
            changes.forEach { it.publish() }
        }
    }

    /** The open database, opened with its rows loaded on the first call. */
    internal suspend fun open(): IDBDatabase {
        opened?.let { return it.await() }
        val deferred = CompletableDeferred<IDBDatabase>().also { opened = it }
        try {
            val database = openDatabase()
            loadTables(database)
            deferred.complete(database)
        } catch (error: Throwable) {
            opened = null
            deferred.completeExceptionally(error)
        }
        return deferred.await()
    }

    internal fun requireUnopened(tableName: String) {
        check(opened == null) { "Table $tableName declared after database $name opened" }
    }

    private suspend fun openDatabase(): IDBDatabase {
        val request = indexedDB.open(name, version.toDouble())
        request.onupgradeneeded = EventHandler {
            val database = request.result
            tables.map { it.name }
                .filterNot { database.objectStoreNames.contains(it) }
                .forEach { database.createObjectStore(it) }
        }
        return request.await().unsafeCast<IDBDatabase>()
    }

    // every request is sent before any is awaited, so the transaction stays open for each
    private suspend fun loadTables(database: IDBDatabase) {
        if (tables.isEmpty()) return
        val transaction = database.transaction(storeNamesOf(tables.map { it.name }), IDBTransactionMode.readonly)
        val requests = tables.map { transaction.objectStore(it.name).getAll() }
        tables.zip(requests).forEach { (table, request) -> loadTable(table, request.await()) }
    }

    private fun <T : Any> loadTable(table: IdbTable<T>, result: JsAny?) {
        table.state.value = result.unsafeCast<Array<String>>()
            .map { idbJson.decodeFromString(table.serializer, it) }
            .associateByTo(LinkedHashMap()) { table.keyOf(it) }
    }
}

private fun storeNamesOf(names: List<String>) =
    names.distinct().map { it.toJsString() }.toTypedArray().unsafeCast<ReadonlyArray<JsString>>()

/** Waits for the transaction to commit, throwing when it aborts. */
private suspend fun IDBTransaction.awaitComplete() = suspendCancellableCoroutine { continuation ->
    oncomplete = EventHandler { continuation.resume(Unit) }
    onabort = EventHandler {
        if (continuation.isActive) continuation.resumeWithException(Error(error?.message ?: "IndexedDB transaction aborted"))
    }
}
