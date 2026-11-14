# Package streetlight.web.idb

## Introduction

A relational layer over the browser's IndexedDB, shaped after Room. An `IdbDb` is the database holder: it declares tables, opens and loads them, and commits writes. A DAO for a domain is implemented over the tables of one `IdbDb`, and its queries are flows derived from `IdbTable.rowsFlow`.

## Dependencies

| Package | Provides |
|---|---|
| `web.idb` (Kotlin wrappers) | IndexedDB |
| `kotlinx.serialization` | JSON rows |
| `streetlight.web.utility` | `IDBRequest.await` |

## Naming

| Type | Name |
|---|---|
| A database built over an `IdbDb` | `FooIdbDb`, implementing the common `FooDb` |
| A DAO over its tables | `FooIdbDao`, implementing the common `FooDao` |

## Declaring Tables

Tables and their foreign keys are declared when the database is constructed, before it first opens. Each table is an object store of the same name, and adding one means raising the version.

```kotlin
private val db = IdbDb("streetlight-foo", version = 1)
val bars = db.table("bars", Bar.serializer()) { it.barId.toString() }
val bazzes = db.table("bazzes", Baz.serializer()) { it.bazId.toString() }
    .references(bars) { it.barId.toString() }
```

A foreign key acts only on removal, which removes each row referencing the removed row, through every level of reference. A save does not check that the parent row exists.

## Rows

Every row of every table is held in memory once the database opens, so a query filters `rowsFlow`, or follows one row with `flowRow`, rather than reaching the store. A query passes its result through `distinctUntilChanged()`, since a table emits on any change to it. `rowsFlow` emits nothing before the rows are loaded, so an empty list means an empty table.

Row order carries no meaning.

## Writes

A write is a batch built in `IdbDb.write`, committed in one transaction across every table it touches, cascades included. The tables emit only after the transaction commits. Writes run one at a time, and a write that has begun finishes despite cancellation.

`IdbTable.save` and `IdbTable.remove` are single writes. Changes that must land together share one `write` block.
