### Introduction

The `streetlight.model` package defines the core domain models and API endpoints for the Streetlight application. It serves as the shared language between the server and client, ensuring consistent data representation and communication patterns across the entire codebase. This package is platform-independent (multiplatform) and forms the foundation for all business logic and data persistence.

### Dependencies

The most common packages this package depends on are:

* `kampfire.api`: For base API node and endpoint definitions (`ApiNode`, `GetEndpoint`, `PostEndpoint`).
* `kampfire.model`: For shared geographical types like `GeoPoint` and `GeoBounds`.
* `kotlinx.serialization`: For JSON serialization and deserialization of data classes.
* `kotlinx.datetime`: For time-based properties using `Instant`.

### Structures

The package follows several key structural patterns to maintain a clean domain model:

* **Endpoint Definitions**: The `Api` object (in `Api.kt`) provides a hierarchical tree of `ApiNode` and endpoint objects, facilitating clear and organized API discovery.
* **Domain Models**: Data classes (in `streetlight.model.data`) are designed with a specific property order: IDs first, followed by non-nullable, nullable, and finally time-based properties.
* **Value Classes for IDs**: Each model typically has a corresponding `FooId` value class that implements `ProjectId` for type-safe identification.
* **Mock Data**: The `MockDb` (in `MockDb.kt`) provides a set of sample data for testing and development of the client-side UI without requiring a live backend.

### Workflows

(This section is initially empty as no specific workflows have been defined yet for this package.)
