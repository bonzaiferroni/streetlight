package kampfire.api

import kotlin.uuid.Uuid

interface TableId<T> {
    val value: T
}

typealias StringId = String
typealias SlugOrId = String
typealias TableUuid = TableId<Uuid>

val TableId<StringId>.isEmpty get() = value.isEmpty()