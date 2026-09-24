package kampfire.api

import kotlin.uuid.Uuid

/** The id of a database record, wrapping its raw [value]. */
interface TableId<T> {
    val value: T
}

typealias StringId = String
/** Text that is either a slug or an id. */
typealias SlugOrId = String
typealias TableUuid = TableId<Uuid>

val TableId<StringId>.isEmpty get() = value.isEmpty()