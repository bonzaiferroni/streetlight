# DAO Query Layers

Adapt the DAOs to the query layers of `docs/packages/streetlight.server.db.tables.md`: join, select, where, order, limit, map, composed by chaining. `EarthDao.readLocationIds` is the model.

## Approach

The older utilities take a wide set of arguments and build a whole query, which keeps the callsite short and the utility tangled. Each is split into a utility per layer it touches, and the callsite chains them.

* A query builder taking `callerId`, such as `eventQuery(callerId)`, becomes the read's own join and `selectWith`, with the star layer's `joinFooStar` and `selectFooStar`.
* `getConstraint` joins on `Op.FALSE` without a caller. A layer skips its join instead.
* A cursor's condition and order become `whereAfterFoo(cursor)` and `orderByFoo(cursor)`.
* A common join between two tables becomes `FooTable.joinWith(BarTable)` in the table file.

## Candidates

* `tables/EventQuery.kt`: `eventQuery`
* `tables/EventLocationQuery.kt`: `eventLocationQuery`
* `tables/LocationAspect.kt`: `locationQuery`
* `tables/LocationLayoutQuery.kt`: `locationLayoutQuery`
* `tables/GalaxyPostAspect.kt`: `query`, `queryCursor`, `joinCaller`, `joinCursor`, `afterCursor`, `orderByCursor`, and `getColumns`
* `services/GalaxyQuery.kt`: `galaxyQuery`
* `services/CityEntityQuery.kt`: `cityEntityQuery` and `pageBy`
* `services/PostTableDao.kt`: `readOrderedPosts`
* `tables/StarIdTable.kt`: `getConstraint`, once nothing uses it

## Open Questions

* Where the shared cursor utilities live once more than one DAO uses them.
