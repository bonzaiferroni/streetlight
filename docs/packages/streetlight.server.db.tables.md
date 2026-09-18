# streetlight.server.db.tables

Exposed table definitions and the queries that read them. One file per table.

## Naming

`FooTable : UuidTable("foo")` — the object name is PascalCase and suffixed `Table`; the
SQL name is in snake case.

`UuidTable` is our go-to table type unless we have a compelling reason to choose a
different id.

## The id and the dto

Rows never leave this package as raw `Uuid` or as Exposed `ResultRow`. Each table has two
companions in `streetlight.model.data`:

- `FooId` — a `@Serializable @JvmInline value class` over `Uuid`, implementing `RecordId`.
  Reading from the table transforms the row id into `FooId`.
- `Foo` — a `@Serializable` dto in `Foo.kt`, holding `FooId` as its first property.

Both live in the same file, `Foo.kt`. Register `FooId` in `RecordId.kt`'s `toRecordId`
map so it can be reified from a `Uuid` or a `String`.

## Column Types

| Kotlin | Column |
|---|---|
| Instant | timestamp |
