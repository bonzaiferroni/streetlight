### Introduction

The `streetlight.model` package defines the core domain models and API endpoints for the Streetlight application. It serves as the shared language between the server and client, ensuring consistent data representation and communication patterns across the entire codebase. This package is platform-independent (multiplatform) and forms the foundation for all business logic and data persistence.

### Dependencies

The most common packages this package depends on are:

* `kampfire.api`: For base API node and endpoint definitions (`ApiNode`, `GetEndpoint`, `PostEndpoint`).
* `kampfire.model`: For shared geographical types like `GeoPoint` and `GeoBounds`.
* `kotlinx.serialization`: For JSON serialization and deserialization of data classes.
* `kotlin.time`: For time-based properties using `Instant`.

### Structures

The package follows several key structural patterns to maintain a clean domain model:

* **Endpoint Definitions**: The `Api` object (in `Api.kt`) provides a hierarchical tree of `ApiNode` and endpoint objects, facilitating clear and organized API discovery.
* **Domain Models**: Data classes (in `streetlight.model.data`) are designed with a specific property order: IDs first, followed by non-nullable, nullable, and finally time-based properties.
* **Value Classes for IDs**: Each model typically has a corresponding `FooId` value class that implements `RecordId` for type-safe identification.
* **Mock Data**: The `MockDb` (in `MockDb.kt`) provides a set of sample data for testing and development of the client-side UI without requiring a live backend.

### Entities

`Entity` is a sealed interface for content shown in a feed, a header, or anywhere else an entity appears. Its members name general content: `label`, `sublabel`, `body`, `image`, `links`. A type maps its own fields onto them, as in `override val body get() = description`, and leaves a member it does not hold at its default.

### Edits

A record the user edits has a `FooEdit` DTO holding the fields a form sends, and `Foo.toEdit()` to start an edit from the record. Its `validity` is a `ValidityCheck` of the `FooProperty` keys that are missing.

### Feeds

An `EntityFeed` is paged with an `EntityCursor`. A cursor holds the sort value and `recordId` of the last entity on the page, so it pages any table keyed by a `Uuid`. A feed of posts takes its `recordId` from the post; a city feed takes it from the location or event.

### Workflows

(This section is initially empty as no specific workflows have been defined yet for this package.)
