# streetlight.server.model

The server's dependency container: the scope interfaces a receiver carries, the facades that group its services, and the interfaces for everything outside the process.

## Scopes

A server-side function takes its dependencies through a receiver rather than a parameter list. The receiver it declares states what it needs.

| Scope | Supplies |
|---|---|
| `DaoScope` | `dao`, `transaction { }`, `log` |
| `ClientScope` | `client` |
| `DataScope` | `DaoScope`, `ClientScope`, `appEmail` |
| `ServerScope` | `DataScope`, `ProviderScope` |
| `ApiScope` | `ServerScope`, Ktor's `Routing` |

Declare the narrowest scope the function uses. A function on `DataScope` is callable from a test that never starts Ktor; the same function on `ApiScope` is not.

`ServerRouting` is the one implementation of `ApiScope`, built in `serveApi` by combining the `ServerScope` with the `Routing` Ktor provides.

## Facades

`DaoFacade` holds one DAO per table, `ClientFacade` holds the external clients. Both take every member as a defaulted constructor parameter, so a caller replaces one and takes the rest.

Reach a DAO through `dao.foo` rather than constructing it. A DAO constructed in place is invisible to the container and cannot be substituted.

## Client Interfaces

Anything outside the process sits behind an interface, with the implementation named for what it talks to.

| Interface | Production | Test |
|---|---|---|
| `MapReferenceClient` | `OSMMapReferenceClient` | `TestMapReferenceClient` |
| `BlobClient` | `S3BlobClient` | `TestBlobClient` |
| `EmailClient` | `PostmarkEmailClient` | `TestEmailClient` |
| `HtmlParserClient` | `KoogHtmlParserClient` | `TestHtmlParserClient` |

The name is the implementation specifier followed by the interface. `HtmlParserClient` is declared in `streetlight.agent` beside the client that implements it; the rest live here.

An interface earns its place where the call leaves the process. Everything inside it is reached by its own type.

## serverModule

`serverModule` is the Koin module. An implementation is registered with `bind` to the interface callers resolve.

```kotlin
single { OSMMapReferenceClient() } bind MapReferenceClient::class
```

The test module in `buildTestServer` registers the same interfaces against the test clients, so a test substitutes a collaborator without any production code knowing.
