# streetlight.server.db.tables

Exposed table definitions and the queries that read them. One file per table.

## Naming

`FooTable : UuidTable("foo")` — the object name is PascalCase and suffixed `Table`; the SQL name is in snake case.

`UuidTable` is our go-to table type unless we have a compelling reason to choose a different id.

## Identifiers and DTOs

Rows never leave this package as raw `Uuid` or as Exposed `ResultRow`. Each table has two companions in `streetlight.model.data`, both in the file `Foo.kt`:

| Type | Definition |
|---|---|
| `FooId` | `@Serializable @JvmInline value class` over `Uuid`, implementing `RecordId` |
| `Foo` | `@Serializable` dto holding `FooId` as its first property |

Reading from the table transforms the row id into `FooId`. Register `FooId` in `RecordId.kt`'s `toRecordId` map so it can be reified from a `Uuid` or a `String`.

## Row Transforms

If a table translates directly to a DTO, define a transform below the object:

| Function | Purpose |
|---|---|
| `ResultRow.toFoo()` | Reads a row into the dto. Where the dto lines up with the columns precisely, this plus `selectAll()` is the whole read |
| `UpdateBuilder<*>.writeFull(foo)` | Writes every column |
| `UpdateBuilder<*>.writeUpdate(foo)` | Writes the columns an update touches |
| `UpdateBuilder<*>.createRecord(foo, callerId)` | Writes the columns set only at creation, then delegates to `writeUpdate` |

## Column Types

| Kotlin | Column |
|---|---|
| Instant | timestamp |

## Manual Inserts

`UuidTable` generates its id in Kotlin, so the column has no database default and a row entered by hand fails the not-null constraint. Give the column a default in an `init` block:

```kotlin
init {
    id.withDefinition("DEFAULT gen_random_uuid()")
}
```

`withDefinition` is an extension on `Column`, which is required here because `UuidTable` declares `id` as a final `override val`. Exposed inserts send their own UUID, so the default applies only to rows entered by hand.

To apply the default to an existing table:

```sql
ALTER TABLE foo ALTER COLUMN id SET DEFAULT gen_random_uuid();
```

Postgres uses a default only when the column is absent from the insert. An explicit `null` is a value and fails the constraint.
