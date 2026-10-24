# Package streetlight.model.testing

## Introduction

Fixtures for tests of code that calls the API, shared by the web client's tests and the app's. They answer calls on a `TestTransport`.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire.api` | `TestTransport` |
| `streetlight.model.io` | `ApiClient` |

## Naming

| Thing | Pattern |
|---|---|
| `answerFoos` | A `TestTransport` extension answering the endpoints of one model, as `answerOfferings` |

## Fixtures

A fixture takes its behavior as parameters defaulted to the ordinary case, as `docs/testing.md` specifies for fakes. A fixture that records what was sent, or holds records a call changes, takes the list as a parameter, so the test holds it and asserts against it.

```kotlin
private val sentBugs = mutableListOf<BugEdit>()

override fun api(): ApiClient = TestApiClient { answerBugs(sentBugs) }
```

`TestApiClient` builds an `ApiClient` whose transport answers every fixture's ordinary case, then the answers its block gives. A later answer for an endpoint replaces the earlier one. A test of one client builds it over `TestTransport` directly.
