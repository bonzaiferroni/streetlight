# Kotlin Style

How Kotlin is written here, beyond what the package documents specify for their own packages.

## Types

A type's name follows an AdjectiveNoun shape, such as `FooBar`.

## Variables

A variable is named from its type, in whole words:

| Case | Name | Example |
|---|---|---|
| The noun alone shadows nothing | The noun | A `FooBar` is `bar` |
| The noun alone shadows another name in scope | The type's name | An `LdEvent` beside an event's `PropertyMap` is `ldEvent` |
| Several values share a type | The noun with the role each plays | `currentBar`, `lastBar`, `cachedBar` |

A name never drops the type for a word that names only where the value came from, such as `declared` for an `LdEvent`, or for a role alone, such as `validated` for a schema.

A `PropertyMap` is named for the record its properties describe, never with `map`: `event`, `location`, and with a role, `pageEvent`, `declaredEvent`, `declaredLocation`.

## Functions

A function's name follows a verbNoun shape and names the noun it acts on.

| Rule | Example |
|---|---|
| The noun is named | `readStoredLocation`, not `readStored` |
| The verb may come from the downstream call that does the work | `mergeAndUpdateLocation` ends in `updateLocation` |
| Two verbs at most | `readOrCreateLocation` |
| A conversion that only reshapes its receiver is `toFoo` | `LdPlace.toPropertyMap()` |
| A function that reads something more is named for that work | `LocationRead.parseLocation(doc)` |
| One kind of work takes one verb, in names and comments alike | A tracker's functions are `track…`, and its comments say "Tracks" |
| A name after its verb follows the AdjectiveNoun shape of a type, naming what the work is about | `trackSkippedUrl`, `trackFailedRecord`, `trackDeclaredLd` |

## Branching

An early return ends a chain of behavior:

```kotlin
val title = edit.title ?: return tracker.recordUnnamed(event)
```

When each path goes on to a call with its own resolution, the paths are branches of a `when`:

```kotlin
when (val locationId = lead.locationId) {
    null -> deliverLocation(lead, location, area, region, declaredName)
    else -> mergeAndUpdateLocation(lead, locationId, location)
}
```

## Builders

A map built from values that may be missing is filled through a builder that leaves them out, not a list of pairs filtered after:

```kotlin
buildPropertyMap {
    this[ParseProperty.Name] = name
    this[ParseProperty.Address] = street
}
```

## Stages

A function body is written in stages, separated by a blank line. The common three:

1. Declare what the context and arguments make available, and decide whether to proceed.
2. Do the work.
3. Do the work that branches on a condition.

```kotlin
val locationRead = schema as? LocationRead ?: return
if (document == null) return

val location = locationRead.parseLocation(document.doc)

when (val locationId = lead.locationId) { ... }
```

A body of more stages, or one whose work is not plain from its name and contents, opens each stage with a short comment. The comment begins with a verb and names what the stage does, as `// fetch and settle schema`; the code is the description. A stage the code makes plain carries none.

A unit of work that recurs across functions is a stage in each function that holds it, and is the shape a shared helper takes.
