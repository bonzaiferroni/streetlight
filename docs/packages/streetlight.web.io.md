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
* **ApiClient**: A facade holding one sub-client per model. Each property is an interface: `api.event` is an `EventClient`, `api.feedback` is a `FeedbackClient`. It mirrors `DaoFacade` on the server.
* **Sub-clients**: One interface per model, named for the model it serves, holding the suspend functions for that model's endpoints in `Api`. `BrowserEventClient` implements `EventClient` against `FetchClient`; `TestEventClient` implements it for a test.
* **WebChatSocket**: Manages a WebSocket connection for real-time chat, providing a reactive `Flow` of incoming messages and a `send` function for outgoing ones.
* **Specialized Clients**: `OSMClient` and `TransitClient` reach external services and feed formats directly rather than through the facade.
* **Protobuf Integration**: Definitions in `ProtobufApi.kt` allow for efficient binary data transfer when interacting with feeds that support the protocol.

### Workflows

CreateApiFunction(Foo):
* Add the suspend function to the `FooClient` interface, returning `Outcome<T>`.
* Implement it in `BrowserFooClient` with `client.getApi()` or `client.postApi()`, passing the `Api` endpoint and any parameters or body.
* Add it to `TestFooClient`.

CreateApiClient(Foo):
* Create `FooClient`, `BrowserFooClient` and `TestFooClient` for the endpoints under `Api.Foos`.
* Add a `foo` property to `ApiClient`, `BrowserApiClient` and `TestApiClient`. The property name is singular.

## Known Issues

The endpoints under `Api.Posts` are served by `PostClient` together with mark and curator endpoints that live under other roots. A client covers a model, not an endpoint root, and these have not been sorted against that rule.
