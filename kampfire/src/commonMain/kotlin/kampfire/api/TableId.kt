package kampfire.api

interface TableId<T> {
    val value: T
}

typealias StringId = String

val TableId<StringId>.isEmpty get() = value.isEmpty()