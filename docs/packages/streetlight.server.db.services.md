# streetlight.server.db.services

The read and write operations over the tables in `streetlight.server.db.tables`. Nothing here knows about HTTP — routes call these.

## Naming

| File | Contents |
|---|---|
| `FooTableDao.kt` | `FooTableDao : DbService()`, the operations owned by one table. The common case |
| `FooQuery.kt` / `FooAspect.kt` | Reads that span tables, or shape a row into something wider than one dto |
| `FooService.kt` | Logic that coordinates daos rather than touching a table directly |

## Dao Methods

Every method is `suspend` and wraps its body in `dbQuery { }`. Reads end in the table file's `ResultRow.toFoo()`; writes pass an `UpdateBuilder` extension from that same file.

```kotlin
suspend fun readByResolution(resolution: MetricResolution, limit: Int = 60) = dbQuery {
    SiteStatusTable.selectAll().where {
        SiteStatusTable.resolution.eq(resolution)
    }.orderBy(SiteStatusTable.id, SortOrder.DESC).limit(limit)
        .map { it.toSiteStatus() }
}
```

Predicates use function notation — `.eq()`, `.greaterEq()` — with infix `and` joining them. `klutch.utils.eq` carries the overloads that take a `RecordId` directly, so an id never has to be unwrapped at the call site.

## Query Windows

A read bounded by time takes the arguments that define the window rather than the window itself, so sibling reads cover the same range without the caller computing it. `readEvents(resolution, limit)` derives `resolution.duration * limit`, matching `readByResolution(resolution, limit)`.

## Table Ownership

A dao may read a second table when that table exists only to serve the first. `SiteStatusTableDao.readEvents` reads `SiteEventTable` because site events have no use outside the status charts. A table with its own routes and lifecycle gets its own dao.

## Logging

A file-private logger at the bottom of the file:

```kotlin
private val log = KotlinLogging.logger(FooTableDao::class)
```
