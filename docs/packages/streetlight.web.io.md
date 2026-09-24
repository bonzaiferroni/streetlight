# streetlight.web.io

## Introduction

The web client's communication with the Streetlight server and with external services.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | `Api` endpoints and DTOs |
| `kampfire.api` | Endpoint and parameter types |
| `kampfire.model` | `Outcome` |
| `kotlinx.coroutines` | Asynchronous calls |

## Naming

| Type | Pattern |
|---|---|
| `FooClient` | The interface holding the calls of one model |
| `BrowserFooClient` | Its implementation against `FetchClient` |
| `TestFooClient` | Its test implementation, in `jsTest` |

## Clients

`ApiClient` is a facade holding one client per model, as `api.event`. It mirrors `DaoFacade` on the server. A client covers a model, not an endpoint root.

`FetchClient` performs every HTTP call to the Streetlight API. A service outside that API, such as OpenStreetMap, has its own client outside the facade.

## Workflows

CreateApiFunction(Foo):
* Add the suspend function to the `FooClient` interface, returning `Outcome<T>`.
* Implement it in `BrowserFooClient` with `client.getApi()` or `client.postApi()`, passing the `Api` endpoint and any parameters or body.
* Add it to `TestFooClient`.

CreateApiClient(Foo):
* Create `FooClient`, `BrowserFooClient` and `TestFooClient` for the endpoints under `Api.Foos`.
* Add a `foo` property to `ApiClient`, `BrowserApiClient` and `TestApiClient`. The property name is singular.

## Known Issues

The endpoints under `Api.Posts` are served by `PostClient` together with mark and curator endpoints that live under other roots. A client covers a model, not an endpoint root, and these have not been sorted against that rule.
