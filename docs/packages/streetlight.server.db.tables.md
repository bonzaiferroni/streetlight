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
| `UpdateBuilder<*>.createFoo(foo, ...)` | Writes the columns set only at creation, then delegates to `updateFoo` |
| `UpdateBuilder<*>.updateFoo(foo)` | Writes the columns an update touches |

`createFoo` writes the id as `this[FooTable.id] = Uuid.random()`. A table may take its id another way, such as from the dto, when its workflow calls for it.

A galaxy's slug is written by `createGalaxy` and by no other function. `updateGalaxy` never writes it.

`Foo` is the name of the concept the function writes. A `Record`, `Row`, or `Edit` suffix on the parameter type is not part of it.

## Column Types

| Kotlin | Column |
|---|---|
| Instant | timestamp |

## Id Type Migrations

A change to a table's id type is a hand-written migration. `generateMigration` renders it as `ALTER COLUMN ... TYPE`, which has no cast between `INT` and `uuid`.

| Step | Detail |
|---|---|
| Add | A new id column on the table and a new reference column on each referencing table |
| Fill | Each reference column through a join on the old id |
| Drop | Column-specific triggers (`UPDATE OF foo_id`), foreign keys, old columns, the old sequence |
| Swap | Rename the new columns into place, then recreate the primary key, foreign keys and indexes under the names Exposed expects |

Startup recreates the dropped triggers.

## Manual Inserts

`UuidTable` generates its id in Kotlin, so the column has no database default and a row entered by hand fails the not-null constraint. Give the column a default in an `init` block:

```kotlin
init {
    id.defaultExpression(CustomFunction("gen_random_uuid", id.columnType))
}
```

`defaultExpression` is used because `UuidTable` declares `id` as a final `override val`. It replaces the client-side default, so `createFoo` writes the id. A default set with `withDefinition` is not visible to `generateMigration`, which then offers to drop it in every migration.

To apply the default to an existing table:

```sql
ALTER TABLE foo ALTER COLUMN id SET DEFAULT gen_random_uuid();
```

Postgres uses a default only when the column is absent from the insert. An explicit `null` is a value and fails the constraint.
