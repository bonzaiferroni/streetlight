package kampfire.api

interface TableId<T> {
    val value: T
}

typealias StringId = String
typealias SlugOrId = String
typealias Slug = String

val TableId<StringId>.isEmpty get() = value.isEmpty()