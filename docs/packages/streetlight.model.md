# Module model

## Introduction

The domain types and API endpoints shared by the server and the web client. Both sides compile the same declarations from `commonMain`.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire.api` | `ApiNode`, the endpoint types, and wire value types such as `Slug` and `Markdown` |
| `kampfire.model` | `Outcome`, `GeoPoint`, and the other shared model types |
| `koala` | `Image`, and the route and content interfaces of `koala.model` |
| `kotlinx.serialization` | Serialization of every type that crosses the wire |

## Naming

| Type | Pattern |
|---|---|
| `Foo` | The DTO of a record, in `streetlight.model.data`, in `Foo.kt` |
| `FooId` | A `@JvmInline value class` over `Uuid` implementing `RecordId`, in `Foo.kt` |
| `FooEdit` | The fields a form sends for `Foo` |
| `FooContent` | The content of a route, implementing `RouteContent` |
| `FooRoute` | A route, in `streetlight.model.ui`, paired with a `Screen` entry |

## Endpoints

`Api` in `Api.kt` is a tree of `ApiNode`s holding one endpoint object per call. The endpoints of one model sit under its node, as `Api.Cities`.

## Data Classes

A DTO lists its id first, then its non-nullable properties, its nullable properties, and its time properties last.

## Entities

`Entity` is a sealed interface for content shown in a feed, a header, or anywhere else an entity appears. Its members name general content: `label`, `sublabel`, `body`, `image`, `links`. A type maps its own fields onto them, as in `override val body get() = description`, and leaves a member it does not hold at its default.

## Edits

A record the user edits has a `FooEdit` DTO holding the fields a form sends, and `Foo.toEdit()` to start an edit from the record. Its `validity` is a `ValidityCheck` of the `FooProperty` keys that are missing.

## Feeds

An `EntityFeed` is paged with an `EntityCursor`. A cursor holds the sort value and `recordId` of the last entity on the page, so it pages any table keyed by a `Uuid`. A feed of posts takes its `recordId` from the post; a city feed takes it from the location or event.
