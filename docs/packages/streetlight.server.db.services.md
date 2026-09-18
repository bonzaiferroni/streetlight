# streetlight.server.db.services

The read and write operations over the tables in `streetlight.server.db.tables`. Nothing
here knows about HTTP — routes call these.

## Naming

- `FooTableDao : DbService()` — the operations owned by one table. The common case.
- `FooQuery.kt` / `FooAspect.kt` — reads that span tables, or shape a row into something
  wider than one dto.
- `FooService.kt` — logic that coordinates daos rather than touching a table directly.

## Shape of a dao method

Every method is `suspend` and wraps its body in `dbQuery { }`. Reads end in the table
file's `ResultRow.toFoo()`; writes pass an `UpdateBuilder` extension from that same file.

```kotlin
suspend fun readByResolution(resolution: MetricResolution, limit: Int = 60) = dbQuery {
    SiteStatusTable.selectAll().where {
        SiteStatusTable.resolution.eq(resolution)
    }.orderBy(SiteStatusTable.id, SortOrder.DESC).limit(limit)
        .map { it.toSiteStatus() }
}
```

Predicates use function notation — `.eq()`, `.greaterEq()` — with infix `and` joining
them. `klutch.utils.eq` carries the overloads that take a `RecordId` directly, so an id
never has to be unwrapped at the call site.

## Ownership

A dao may reach past its own table when the second table exists only to serve the first.
`SiteStatusTableDao.readEvents` reads `SiteEventTable` because site events have no life
outside the status charts; a table with its own routes and lifecycle gets its own dao.

## Logging

A file-private logger at the bottom of the file:

```kotlin
private val log = KotlinLogging.logger(FooTableDao::class)
```
