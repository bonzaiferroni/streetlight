### Introduction

The `streetlight.server.routes` package contains the Ktor routing definitions for the Streetlight server. It is responsible for mapping incoming HTTP requests to either HTML-rendered pages or API endpoint implementations. It acts as the controller layer, bridging the domain model (defined in `streetlight.model`) and the database services (defined in `streetlight.server.db.services`).

### Dependencies

The most common packages this package depends on are:

* `io.ktor.server.routing`: For defining the routing structure.
* `io.ktor.server.html`: For responding with HTML content using the Kotlin HTML DSL.
* `klutch.server`: For mapping `ApiNode` and `Endpoint` objects from `streetlight.model.Api` to server-side logic using `getEndpoint`, `postEndpoint`, etc.
* `streetlight.model`: For API definitions and shared data models.
* `streetlight.server.db.services`: For interacting with the database through DAOs and Services.
* `streetlight.web.pages`: For the server-side rendered HTML templates.

### Structures

The package follows several consistent patterns for defining server-side logic:

* **Serve Functions**: Functions named `Routing.serveFoos(app: ServerProvider)` are extension functions on Ktor's `Routing` class. They group related endpoints and are typically invoked in a central `RoutingApi.kt` or `servePages.kt` file.
* **Endpoint Mapping**: The package uses `klutch.server` utilities to implement the endpoints defined in `Api.kt`. This ensures that the server-side implementation remains in sync with the client-side expectations.
* **HTML Rendering**: For traditional web pages (like the home page or specific profile pages), the package uses `respondHtml` to call templates defined in the `streetlight.web.pages` package.
* **Authentication**: Sensitive endpoints are often wrapped in `authenticateJwt { ... }` blocks to ensure that only authorized users can access or modify data.

### Workflows

CreateServeFunction(Foo):
* Create the function `Routing.serveFoos(app: ServerProvider = RuntimeProvider) { }` in a new file `serveFoos.kt` in the package `streetlight.server.routes`.
* Inside the function, define endpoints using `getEndpoint`, `postEndpoint`, etc., mapping them to the corresponding `Api.Foo` definitions.
* Add an invocation to `serveFoos()` in `RoutingApi.kt` or the appropriate entry point.
