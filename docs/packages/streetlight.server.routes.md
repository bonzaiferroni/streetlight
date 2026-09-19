# streetlight.server.routes

Implementations of the endpoints declared in `streetlight.model.Api`. One realm of the application per file.

## File Naming

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

## Page Routes

`servePages.kt` responds with HTML from `streetlight.web.pages` instead of implementing `Api` endpoints. It takes a `ServerResource` because the templates need build-mode-dependent asset paths.
