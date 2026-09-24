# Package streetlight.server.plugins

## Introduction

Ktor installation and configuration, and the assembly of the routing tree.

## Dependencies

| Package | Provides |
|---|---|
| `io.ktor.server` | Plugins and routing |
| `streetlight.server.model` | `ServerScope` and `DaoFacade` |
| `streetlight.server.routes` | Serve functions |

Two kinds of member live here, separated by receiver. A function on `Application` configures the server. A function on `ApiScope` serves endpoints, and follows the conventions in `streetlight.server.routes.md`.

## Configure Functions

A configure function is `fun Application.configureFoo()`, holds one concern, and installs its Ktor plugin. It takes `ServerScope` as a parameter when it needs the server's services, because `Application` does not carry them.

Every configure function is called from `Application.module()`. That function is the whole order of startup, and the order is load-bearing:

| Position | Reason |
|---|---|
| `configureRateLimits` before `configureAuth` | Rate limit keys read the session principal |
| `configureSerialization` before `serveApi` | Endpoints negotiate content on registration |
| `configureDatabases` before `serveApi` | Endpoint bodies reach a live connection |
| `configureTransit` after `configureDatabases` | The transit load reads and writes tables |
| `serveApi` last | Routing closes over everything installed before it |

A file holds one configure function, and is named either for that function (`configureMetrics.kt`) or for the concern it configures (`Cors.kt`, `Serialization.kt`).

## Startup Switches

`Application.streetlightModule` reads `ServerConfig` and calls a configure function only when its switch is on. `configureRateLimits` always installs the plugin and takes its limit from the switch.

## serveApi

`serveApi.kt` is the join between the two kinds of member. It opens `routing { }`, constructs a `ServerRouting` from the `ServerScope` and the `Routing`, and calls every serve function in the application with that as receiver.

A serve function that is not called here serves nothing. The compiler does not report the omission, because an uncalled extension function is valid.

The catch-all that answers `404` under `Api.path` is registered after the serve functions. Ktor matches in registration order, so a route added below it is unreachable.

## Serve Functions

These serve functions live here rather than in `streetlight.server.routes`:

| Function | File | Serves |
|---|---|---|
| `serveBug` | `serveBug.kt` | `Api.Bugs` |
| `serveFeedback` | `serveFeedback.kt` | `Api.Feedback` |
| `serveSiteStatus` | `serveSiteStatus.kt` | `Api.Status` |
| `serveWebhooks` | `configureWebhooks.kt` | A raw `post` path, not an `Api` endpoint |

`serveWebhooks` does not take its path from `Api` because the caller is Postmark, not the Streetlight client. It authenticates on a secret path segment read from the environment through `provide`, and answers `200` before doing its work, so that a slow write does not provoke a retry from the sender.

## Daemons

A daemon is a background worker on an interval, named `FooDaemon`. It is launched from the configure function of the concern it serves, so that the thing it depends on is installed before it starts, as `configureDatabases` launches `TableDaemon` after `initDb`.

`configureTransit` launches the GTFS load in the application's own scope, so stopping the application cancels it. Startup work belongs in a configure function rather than a serve function.
