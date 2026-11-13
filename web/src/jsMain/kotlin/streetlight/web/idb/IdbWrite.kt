package streetlight.web.idb

import kotlinx.serialization.json.Json
import web.idb.IDBTransaction
import web.idb.IDBValidKey

/** A batch of writes to the [tables] of a database, committed together by [IdbDb.write]. */
class IdbWrite internal constructor(
    private val tables: List<IdbTable<*>>,
) {
    private val changes = mutableMapOf<IdbTable<*>, IdbChanges<*>>()

    internal val touchedChanges get() = changes.values.filter { it.touchedKeys.isNotEmpty() }

    /** Stores [value] in [table] in place of the row under its key. */
    fun <T : Any> save(table: IdbTable<T>, value: T) {
        changesOf(table).save(value)
    }

    /** Removes the row under [key] from [table], and each row that references it through a foreign key. */
    fun <T : Any> remove(table: IdbTable<T>, key: String) {
        if (!changesOf(table).remove(key)) return
        tables.forEach { child -> removeChildren(child, table, key) }
    }

    private fun <C : Any> removeChildren(child: IdbTable<C>, parent: IdbTable<*>, key: String) {
        val foreignKeys = child.foreignKeys.filter { it.parent == parent }
        if (foreignKeys.isEmpty()) return

        val childKeys = changesOf(child).rows
            .filterValues { row -> foreignKeys.any { it.parentKeyOf(row) == key } }
            .keys.toList()
        childKeys.forEach { remove(child, it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> changesOf(table: IdbTable<T>) =
        changes.getOrPut(table) { IdbChanges(table) } as IdbChanges<T>
}

/** The rows of [table] as a write leaves them, and the keys it touched. */
internal class IdbChanges<T : Any>(
    private val table: IdbTable<T>,
) {
    val rows = LinkedHashMap(table.loadedRows)
    val touchedKeys = mutableSetOf<String>()

    fun save(value: T) {
        val key = table.keyOf(value)
        rows[key] = value
        touchedKeys += key
    }

    /** Removes the row under [key], returning whether there was one. */
    fun remove(key: String): Boolean {
        if (rows.remove(key) == null) return false
        touchedKeys += key
        return true
    }

    /** Sends a request for each touched key to [transaction], without awaiting any. */
    fun request(transaction: IDBTransaction) {
        val store = transaction.objectStore(table.name)
        touchedKeys.forEach { key ->
            when (val row = rows[key]) {
                null -> store.delete(IDBValidKey(key))
                else -> store.put(idbJson.encodeToString(table.serializer, row).toJsString(), IDBValidKey(key))
            }
        }
    }

    fun publish() {
        table.state.value = rows
    }

    val tableName get() = table.name
}

/** Reads rows that have changed shape: unknown keys are skipped and unknown enum values take their default. */
internal val idbJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
