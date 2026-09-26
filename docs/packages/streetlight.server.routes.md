# Package streetlight.server.routes

## Introduction

Implementations of the endpoints declared in `streetlight.model.Api`. One realm of the application per file.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | `Api` endpoints and DTOs |
| `streetlight.server.model` | `ApiScope` and content reads |
| `streetlight.server.db.datascope` | Operations that span daos |
| `klutch.server` | `authGate`, `getApi` and `postApi` |
| `kampfire.model` | `Outcome` |

## Naming

A file is named `serveFoo.kt` and holds one function, `fun ApiScope.serveFoo()`. The name matches the `Api` subtree it implements, so `Api.Feedback` is implemented by `serveFeedback`.

Every serve function is registered by a call in `serveApi.kt`. A function that is written but not registered serves nothing, and the compiler does not report it.

## ApiScope

`ApiScope` is the receiver on every serve function. It combines Ktor's `Routing` with `ServerScope`, so an endpoint body reaches both the routing DSL and the server's services without taking either as a parameter.

`ServerScope` supplies:

| Member | Holds |
|---|---|
| `dao` | `DaoFacade`, one property per table's DAO |
| `client` | `ClientFacade`, the external service clients |
| `transaction { }` | A suspending Exposed transaction |
| `log` | The logger scoped to data work |

Take nothing through a parameter that the scope already supplies. The scope exists so that a serve function's signature stays empty and a new service becomes available to every endpoint at once.

Anything else comes from `provide<T>()`, resolved by the interface rather than the implementing class. `provide<MapReferenceClient>()` is served by the real client in production and the test client under test; `provide<OSMMapReferenceClient>()` is served only where the implementation is registered.

A class a serve function depends on takes its collaborators through its constructor, so that the same substitution reaches it.

## Endpoint Bodies

`getApi` and `postApi` bind an endpoint object from `Api` to its implementation. Binding to the object rather than to a path string means a renamed or moved endpoint fails to compile on both sides at once.

An endpoint body returns an `Outcome`. It does not write to the call directly.

```kotlin
fun ApiScope.serveFoo() {
    authGate {
        postApi(Api.Foo.Create) {
            val callerId = call.getIdentity().callerId
            dao.foo.create(it.data, callerId).toOutcome()
        }
    }
}
```

## Authentication

Endpoints sit inside an `authGate` block rather than carrying a per-endpoint check.

| Block | Caller | Identity accessor |
|---|---|---|
| `authGate { }` | Required | `call.getIdentity()` |
| `authGate(optional = true) { }` | Optional | `call.getIdentityOrNull()` |

A serve function opens a separate `authGate` block for each of these when it holds endpoints of both kinds. Grouping by gate rather than by endpoint keeps the authentication requirement visible at the block, where it cannot be missed by a reader skimming for it.

## Edit Permission

A record with no owner, such as a city, or a location or event with no host, is editable by any signed-in user. Its update endpoint sits in `authGate { }` and checks nothing further about the caller.

## Content Endpoints

Every route that has a shell has a content endpoint returning the result of `readFooContent`, so the endpoint and `renderFoo` read the same content. The endpoint of a route whose content belongs to an `Api` node, such as `Api.Galaxies.ReadContent` or `Api.Cities.ReadContent`, is served with that node. `serveContent.kt` implements `Api.Content`, which holds the rest.

## Page Routes

`servePages.kt` responds with HTML from `streetlight.web.pages` instead of implementing `Api` endpoints. It takes a `ServerResource` because the templates need build-mode-dependent asset paths.

A screen whose route is likely to be shared or linked from another site is rendered here on the initial load. Its branch in `renderScreen` calls `renderFoo`, an `ApiScope` extension returning `HtmlRender`, which reads the route's content with `readFooContent` and passes it to `fooShell` inside `appPage`. A screen with no branch is rendered by `renderClientBase`, and the browser builds its content.

`readFooContent` is a `DaoScope` extension in `streetlight.server.model`.

## LM Instructions

`SchemaParserText` and `ParserText` hold the instructions sent to the language model. They are written for a capable model and state what is wanted, not how one model tends to go wrong. A mistake a particular model makes, such as malformed CSS, is caught by validation rather than answered with an instruction, so the instructions carry over to another model.
