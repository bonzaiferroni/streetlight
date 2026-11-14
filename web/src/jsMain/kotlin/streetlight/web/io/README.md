# Package streetlight.web.io

## Introduction

The web client's transport to the Streetlight server, the services built on its streams, and the clients of external services.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.io` | `ApiClient` |
| `streetlight.model` | `Api` endpoints and DTOs |
| `kampfire.api` | `ApiTransport` and the endpoint and parameter types |
| `kampfire.model` | `Outcome` |
| `kotlinx.coroutines` | Asynchronous calls and streams |

## Transport

`WebTransport` is the browser's `ApiTransport`: calls go through `fetch`, event streams through `EventSource`, and sockets through `WebSocket`. Its cousin in the app is `AppTransport`.

A call that only the browser makes, such as the GTFS protobuf feed `TransitClient` reads, is a member of `WebTransport` and is reached through a client outside `ApiClient`.

## Streams

A service that follows a stream, such as `TalkLog` or `OmniClient`, collects the `Flow` or `MessageSocket` its client returns in its scope. Cancelling the collection closes the stream.
