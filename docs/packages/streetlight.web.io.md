### Introduction

The `streetlight.web.io` package handles all external communication for the Streetlight web client. It provides the infrastructure for HTTP requests, WebSocket connections, and specialized clients for interacting with both internal and external APIs (like OpenStreetMap). This package encapsulates the complexities of data serialization, authentication headers, and network protocols, exposing clean, high-level interfaces to the rest of the application.

### Dependencies

The most common packages this package depends on are:

* `streetlight.model`: For API endpoint definitions and shared models.
* `streetlight.model.data`: For domain data structures used in serialization.
* `kampfire.api` and `kampfire.model`: For shared user management and common data patterns.
* `kotlinx.serialization`: For JSON and Protobuf serialization/deserialization.
* `kotlinx.coroutines`: For asynchronous network operations.
* `koala.external`: For shared external definitions.

### Structures

The package uses several key classes and patterns to manage I/O:

* **FetchClient**: The core engine for HTTP communication. It handles method resolution, authentication (via `UserCred`), and response decoding (supporting both JSON and Protobuf).
* **ApiClient**: A high-level, service-oriented wrapper around `FetchClient` that exposes suspend functions for all backend endpoints defined in `Api`.
* **WebChatSocket**: Manages a WebSocket connection for real-time chat, providing a reactive `Flow` of incoming messages and a `send` function for outgoing ones.
* **Specialized Fetch Clients**: Classes like `OSMFetchClient` and `TransitBrowserClient` provide targeted functionality for external services or specific data types (like GTFS feeds).
* **Protobuf Integration**: Definitions in `ProtobufApi.kt` allow for efficient binary data transfer when interacting with feeds that support the protocol.

### Workflows

CreateApiFunction(Foo):
* Add a new suspend function to `ApiClient` that maps to the corresponding `Api` endpoint.
* Use `client.get()` or `client.post()` within the function, passing the endpoint and any required parameters or body.
