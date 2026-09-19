# Testing

How tests are written in this project. Fixtures belonging to one package are documented in that package's document under `docs/packages/`.

## Voice

A test name is written in plain, direct language. It names who acts, what they do, and what becomes true. It carries no theme, no metaphor, and no in-joke.

```kotlin
fun `a guest submits feedback and it is stored`()
fun `a second verification request consumes the first token`()
fun `a verified address survives a wrong password`()
```

Test names are read by people outside this project: a contributor, a reviewer, a failure report in CI. A themed name costs that reader a translation step and tells them nothing about the code. The same applies to assertion messages and to comments inside a test.

This rule covers test code. It says nothing about how the crew talks to each other.

## Kinds of Test

| Kind | What is real | Entry point | Scope |
|---|---|---|---|
| Unit | One function or type | A direct call | A rule that holds with no collaborators |
| Scenario | The server, its database, and its services | A server-side function, below HTTP | What the server does with a request |
| End-to-end | The whole stack, including the browser | A user action in a rendered page | Behavior that crosses between client and server |

An end-to-end test covers a crossing: a request leaves the browser and an effect lands in the database. Behavior that resolves on one side belongs to the kind that tests that side.

A guard that stops a request before it is sent is client behavior. Its rule is a unit test on the type that holds it.

Reach for the cheapest kind that can fail for the reason being tested.

A shared model is a reason to write fewer end-to-end tests than a split stack would need. The client and server hold the same DTO types, so a disagreement about the shape of a request fails to compile.

## Assertions

Assert the state the user would care about, not the call that produced it. A test that proves mail was delivered to an inbox outlives a refactor that a test proving `sendEmail` was called does not.

An assertion that can fail for more than one reason carries a message naming what was expected.

```kotlin
assertEquals(2, emailRouter.count(email), "both requests should have sent mail")
```

Assert the absence as well as the presence when a flow decides between the two. A record that must be written and must not be shown needs both halves stated, or the test passes when either is wrong.

## Utilities

Keep the set of test utilities tight and compose longer arrangements from shorter ones.

Add a utility when a test repeats an arrangement, not in anticipation of one. A utility that renames a single call costs more than it saves: a reader now has two names for one thing and must learn which is which.

A utility may assert as part of its work, so that every test using it also proves the condition it depends on.

## Fakes

A collaborator is replaced by a fake that holds real behavior, not by a mock that records calls. A fake lets a test assert against resulting state, which is what the test is about.

A fake method that no test needs is left as `TODO()`. A test that reaches it fails loudly rather than passing on a fabricated return.

## Controlling a Fake

A fake takes its behavior as a constructor parameter, defaulted to the ordinary case.

```kotlin
TestEmailClient(emailRouter, getErrorCode = { 406 })
```

The test that needs the unusual behavior passes it. Every other test takes the default and says nothing.

Give the parameter the narrowest shape that serves the test: a value where one answer is enough, a function where the answer changes between calls.

A fake with no configured answer fails with a message naming what was asked for.

## Isolation

A test starts from an empty database and assumes nothing left behind by another. Expensive fixtures may be shared across a run; data may not.

A test that needs one collaborator to behave unusually builds its own server with that one collaborator replaced, rather than changing a fixture other tests rely on.
