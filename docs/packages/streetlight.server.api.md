# streetlight.server.api

Tests that enter the server through an HTTP request and assert on the database. There is no browser and no client code: the request is built from an `Api` endpoint and sent to the real routing tree over a test client.

`docs/testing.md` states how tests are written. This document states what this package provides.

The package sits between `streetlight.server.integration`, which enters below HTTP by calling server-side functions, and `streetlight.server.e2e`, which enters through a browser. A test belongs here when the routing, authentication or serialization of a request is part of what it proves.

## ApiTest

`ApiTest` extends `DatabaseTest`, so it inherits the shared container and the truncate before each test.

`runApiTest { }` runs its block inside Ktor's `testApplication`, with `streetlightModule` installed over the same `TestServer` that `DatabaseTest` builds for the test. The block's receiver is an `ApiTestScope`.

The server is built by `buildTestServer`, so its `ServerConfig` has metrics, database setup, transit and rate limits off. A test that needs another configuration replaces `server` with `buildTestServer(serverConfig = ...)` before calling `runApiTest`.

## Calling

| Function | Sends |
|---|---|
| `postApi(endpoint, body)` | A JSON body to a `PostEndpoint` |
| `getApi(endpoint)` | A request to a `GetEndpoint` |

Each returns an `Outcome`. A `200` response is decoded from the CBOR `Outcome` the server writes. Any other status becomes a `Problem`, named for `401`, `409`, `429` and `500` and carrying `HTTP error: <status>` otherwise.

`toDataOrThrow()` and `toProblemOrThrow()` unwrap the result as they do in `streetlight.server.integration`.

## Arriving Signed In

`signIn(session)` puts the session cookie in the test client. A test that needs a signed-in user registers and logs in server-side, then hands the session over.

```kotlin
server.registerStar()
signIn(server.loginStar())
```

The client keeps the cookie for every later call in the block.

## Asserting

A test asserts on the table, not on a response that echoes the request. `latestBugRowOrNull` and `bugCount` in `BugUtility.kt` read `BugTable` for this, and other tables get readers of the same shape.

Assert what the server decides: identity, ownership, uniqueness, and the columns the server stamps. A rule that only stops user error is enforced on the client and is not tested here.
