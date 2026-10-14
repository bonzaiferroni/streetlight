# Package streetlight.server.model

## Introduction

The server's dependency container: the scope interfaces a receiver carries, the facades that group its services, the interfaces for everything outside the process, and the DTOs the server keeps to itself.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.db.dao` | The daos in `DaoFacade` |
| `klutch.server` | Providers and identity |
| `streetlight.model.data` | DTOs |
| `org.koin` | The container |

## Server DTOs

A DTO the server uses internally, such as one holding columns the UI never reads, is declared here rather than in `streetlight.model.data`. Its id is declared beside it, as `streetlight.server.db.tables.md` specifies.

## Scopes

A server-side function takes its dependencies through a receiver rather than a parameter list. The receiver it declares states what it needs.

| Scope | Supplies |
|---|---|
| `DaoScope` | `dao`, `transaction { }`, `log` |
| `ClientScope` | `client` |
| `DataScope` | `DaoScope`, `ClientScope`, `appEmail`, `hostAddress` |
| `ServerScope` | `DataScope`, `ProviderScope` |
| `ApiScope` | `ServerScope`, Ktor's `Routing` |

Declare the narrowest scope the function uses. A function on `DataScope` is callable from a test that never starts Ktor; the same function on `ApiScope` is not.

`ServerRouting` is the one implementation of `ApiScope`, built in `serveApi` by combining the `ServerScope` with the `Routing` Ktor provides.

## Facades

`DaoFacade` holds the daos, each a defaulted constructor parameter, so a caller replaces one and takes the rest. `ClientFacade` holds the external clients, resolved from the container; `buildTestServer` takes each client as a defaulted parameter, so a test replaces one and takes the rest. `StreetlightAgent` holds the identity the server and the crawler present when they fetch from other sites.

Reach a DAO through `dao.foo` rather than constructing it. A DAO constructed in place is invisible to the container and cannot be substituted.

## Client Interfaces

Anything outside the process sits behind an interface, with the implementation named for what it talks to.

| Interface | Production | Test |
|---|---|---|
| `MapReferenceClient` | `OSMMapReferenceClient` | `TestMapReferenceClient` |
| `BlobClient` | `S3BlobClient` | `TestBlobClient` |
| `EmailClient` | `PostmarkEmailClient` | `TestEmailClient` |
| `PaymentClient` | `StripePaymentClient` | `TestPaymentClient` |

The name is the implementation specifier followed by the interface, and the interfaces live here.

An interface and the types it returns are named for the domain, not the provider, as `PaymentClient` and `ProviderAccount`. Every key the server and the daemon read from the environment is a constant of `EnvKey` in `streetlight.server`, read as `env.read(EnvKey.FOO)`. The implementation reads its secret from the environment on first call, so a server without the key starts and fails only the calls that need it.

An interface earns its place where the call leaves the process. Everything inside it is reached by its own type.

## Outbound Links

A link sent out of the site, such as in an email or to a payment provider, is built as `"$hostAddress${route.toRelativePath()}"`, never with `toAbsolutePath()`.

## serverModule

`serverModule` is the Koin module. An implementation is registered with `bind` to the interface callers resolve.

```kotlin
single { OSMMapReferenceClient() } bind MapReferenceClient::class
```

The test module in `buildTestServer` registers the same interfaces against the test clients, so a test substitutes a collaborator without any production code knowing.

## ServerConfig

`ServerConfig` holds the switches that decide which parts of the server start. It is provided through the container like any other service, and `streetlightModule` reads it with `provide<ServerConfig>()`.

| Switch | Off means |
|---|---|
| `withMetrics` | No metrics daemon |
| `withDatabase` | The module does not connect to the database |
| `withTransit` | No GTFS load |
| `withRateLimits` | The limiters are registered with an unlimited bucket |

Every switch defaults to `true`. A test provides its own `ServerConfig` through `buildTestServer`.
