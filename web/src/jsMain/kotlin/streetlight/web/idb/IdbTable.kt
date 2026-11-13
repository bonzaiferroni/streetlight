package streetlight.web.idb

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer

/**
 * A table of [db] kept in the object store [name], holding each [T] as JSON under the key [keyOf] gives it.
 *
 * Declared with [IdbDb.table].
 */
class IdbTable<T : Any> internal constructor(
    private val db: IdbDb,
    val name: String,
    internal val serializer: KSerializer<T>,
    internal val keyOf: (T) -> String,
) {
    internal val state = MutableStateFlow<Map<String, T>?>(null)
    internal val foreignKeys = mutableListOf<IdbForeignKey<T>>()

    /** Each row, emitted once [db] opens and again after each write that changes them. */
    val rowsFlow: Flow<List<T>> = flow {
        db.open()
        emitAll(state.filterNotNull().map { it.values.toList() })
    }

    /** Each row as it stands. */
    suspend fun readRows(): List<T> {
        db.open()
        return loadedRows.values.toList()
    }

    /** The row under [key], or `null` when there is none. */
    suspend fun read(key: String): T? {
        db.open()
        return loadedRows[key]
    }

    /** Stores [value] in place of the row under its key. */
    suspend fun save(value: T) = db.write { save(this@IdbTable, value) }

    /** Removes the row under [key], and each row that references it through a foreign key. */
    suspend fun remove(key: String) = db.write { remove(this@IdbTable, key) }

    /**
     * Declares that each row references the row of [parent] under the key [parentKeyOf] gives, or no row for `null`.
     * Removing the parent row removes each row that references it.
     */
    fun references(parent: IdbTable<*>, parentKeyOf: (T) -> String?): IdbTable<T> {
        db.requireUnopened(name)
        foreignKeys += IdbForeignKey(parent, parentKeyOf)
        return this
    }

    internal val loadedRows: Map<String, T>
        get() = state.value ?: error("Table $name read before its database opened")
}

/** A reference from a row to the row of [parent] under the key [parentKeyOf] gives. */
internal class IdbForeignKey<T>(
    val parent: IdbTable<*>,
    val parentKeyOf: (T) -> String?,
)
