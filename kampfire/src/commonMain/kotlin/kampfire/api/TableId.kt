package kampfire.api

interface TableId<T> {
    val value: T
}

val TableId<String>.isEmpty get() = value.isEmpty()