# streetlight.server.integration

Integration tests for the server. Each file covers one user-facing flow from its entry point to the state it leaves behind.

Shared fixtures and utilities live one package up, in `streetlight.server` of the test source set. Fakes for the injected clients live in `streetlight.server.model` of the test source set.

`docs/testing.md` states how tests are written. This document states what this package provides.

## Database

`DatabaseTest` is the base class for any test that touches the database. It holds one PostgreSQL container for the whole run, raises the schema once, and truncates every table before each test.

| Scope | What |
|---|---|
| Once per run | Container start, connection, schema |
| Once per test | Truncate all tables, fresh `TestServer` |

The container is the expensive thing and the data is the thing that must not leak between tests, so the container is shared and the data is not.

## Building a Server

`buildTestServer` takes every collaborator as a defaulted parameter. A test that needs one collaborator to behave differently overrides that one and takes the rest.

```kotlin
val bouncingServer = buildTestServer(
    emailClient = { TestEmailClient(it, getErrorCode = { 406 }) },
)
```

This keeps the unusual condition of a scenario visible in the test that depends on it, rather than in a fixture shared with tests that do not.

## Scope

A test body opens `with(server) { }` and calls the same receiver-scoped functions the server calls. Test code and production code then read alike, and a function that moves into or out of `ServerScope` breaks the test at compile time.

## Outcomes

An `Outcome` is unwrapped by stating which side is expected.

| Function | Expects | On the other side |
|---|---|---|
| `toDataOrThrow()` | `Ok` | Fails with the `Problem` message |
| `toProblemOrThrow()` | `Problem` | Fails with the unexpected data |

Neither is a convenience for reaching the value. Each is an assertion, so a call that takes the wrong branch fails at that line rather than further down.

## Utilities

Utilities live in `TestUtility.kt`.

| Function | Gives |
|---|---|
| `registerStar` | A registered account |
| `registerVerifiedStar` | `registerStar`, then redeems the mailed token |
| `registerAdmin` | `registerStar`, then grants the `Admin` role |
| `latestMail` | The last mail delivered to an address |
| `extractToken` | The token in a mail, for a given screen |
| `loginStar` | A live session |
| `signupRequestOf` | A `SignUpRequest` with defaults |
| `sessionCountOf` | The number of sessions held by an account |
| `latestAuthTokenOrNull` | The newest token of a type for an account |

`TestDefault` holds one canonical account. A test that does not care about the identity takes the defaults, so any value written out in a test is a value that test depends on.

## Fakes

`EmailRouter` keeps an inbox per address. `TestEmailClient` delivers into it, and takes a `getErrorCode` function so a test can make sending fail. `TestMapClient` and `TestBlobClient` are unimplemented.
