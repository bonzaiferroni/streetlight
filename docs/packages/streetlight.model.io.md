# Package streetlight.model.io

## Introduction

The clients of the Streetlight API, shared by the web client and the app. Each platform supplies the `ApiTransport` they call through.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | `Api` endpoints and DTOs |
| `kampfire.api` | `ApiTransport`, `getApi`, `postApi`, `postForm`, `openEvents`, `openSocket` and `MessageSocket` |
| `kampfire.model` | `Outcome` |

## Naming

| Type | Pattern |
|---|---|
| `FooClient` | A class holding the calls of one model, taking an `ApiTransport` |

## Clients

`ApiClient` is a facade holding one client per model, as `api.event`, all over one transport. It mirrors `DaoFacade` on the server. A client covers a model, not an endpoint root.

A call is one line, declaring its return type and delegating to a transport wrapper with the `Api` endpoint and any id, parameters or body. A call that streams returns a `Flow` from `openEvents` or a `MessageSocket` from `openSocket`.

A service outside the Streetlight API, such as OpenStreetMap, has its own client outside the facade, in the platform's own `io` package.

## Workflows

CreateApiFunction(Foo):
* Add the suspend function to `FooClient`, returning `Outcome<T>`, with `transport.getApi()` or `transport.postApi()`.
* When a test needs it answered, add the answer to the model's fixture in `streetlight.model.testing`.

CreateApiClient(Foo):
* Create `FooClient` for the endpoints under `Api.Foos`.
* Add a `foo` property to `ApiClient`. The property name is singular.

## Known Issues

The endpoints under `Api.Posts` are served by `PostClient` together with mark and curator endpoints that live under other roots. A client covers a model, not an endpoint root, and these have not been sorted against that rule.
