# streetlight.server.e2e

End-to-end tests. A test here drives a real browser against a real server and a real database, and asserts on both what the page shows and what the database holds.

`docs/testing.md` states how tests are written. This document states what this package provides.

The package is distinct from `streetlight.server.integration`, which enters below HTTP by calling server-side functions directly. A test belongs here only when a browser is required to make it fail for the reason it is testing.

## BrowserTest

`BrowserTest` extends `DatabaseTest`, so it inherits the shared container and the truncate before each test.

| Scope | What |
|---|---|
| Once per run | Playwright, one Chromium browser |
| Once per test | A Ktor server on a free port, a fresh `BrowserContext` and `Page` |

The browser process is the expensive thing and the cookies are the thing that must not leak, so the browser is shared and the context is not.

The server is started per test because it closes over the `TestServer` that `DatabaseTest` rebuilds for each one. It runs on port `0` and reports the port it was given, so tests never contend for a fixed one.

It is started with the `ServerConfig` that `buildTestServer` provides, which turns metrics, database setup, transit and rate limits off. `streetlight.server.model.md` states what each switch does.

Set `E2E_HEADED=true` to watch a test run in a visible browser.

## Selectors

Address an element by the role and name a user would perceive, not by a class or a position.

| Helper | Finds |
|---|---|
| `editor(label)` | A `textbox` role with that accessible name |
| `button(label)` | A `button` role with that accessible name |
| `tab(label)` | The first element with that exact text |
| `dropMenu(label)` | The `select` inside the block with that label |

`markdownEditor` sets `role` and `aria-label` from the label it is given, so the accessible name is a handle the markup already provides. A selector built on a class or a child index breaks when the layout changes, and a class change should not fail a test about behavior.

## Acting

| Helper | Does |
|---|---|
| `openScreen(screen)` | Navigates to the screen's path on the test server |
| `openTab(label)` | Clicks a tab |
| `writeIn(label, text)` | Types into an editor and asserts it holds the text |
| `clickButton(label)` | Clicks a button |
| `awaitMessage(text)` | Waits for the text to become visible |
| `chooseIn(label, option)` | Selects an option in a drop menu |
| `awaitEditorCleared(label, text)` | Waits for an editor to let go of the text |
| `awaitText(text)` | Waits for the text to appear on the page |
| `awaitNoText(text)` | Waits for the text to be absent |
| `signIn(session)` | Puts a session cookie in the browser context |

Reload the page only where the test is about what survives a reload.

`writeIn` asserts before returning. A contenteditable that did not receive the typed text would otherwise fail later at the result, where the symptom is identical to a broken endpoint.

`awaitMessage` waits rather than sleeping. A fixed delay is either too short and flakes or too long and wastes the run.

`awaitNoText` needs an anchor. Assert that something which should be there is visible before asserting that something else is absent, or the absence passes against a page that has not finished rendering.

## Arriving Signed In

`signIn` takes a `Session` minted by the integration utilities and writes it into the `BrowserContext` as the session cookie. A test that needs a signed-in user registers and logs in server-side, then hands the session to the browser.

```kotlin
val session = runBlocking {
    server.registerStar()
    server.loginStar()
}
signIn(session)
```

Clicking through the sign-in form tests the sign-in form.

## Seeding

State a test depends on, but does not create through the page, is written through the DAO before the page opens. A seeded row is the anchor that proves a feed rendered, and the only way to place a row the browser cannot produce.

## Reading the Database

A test asserts on the table rather than on the rendered feed when the flow's effect is not meant to be visible. `latestFeedbackOrNull`, `latestDeviceAgentOrNull` and `feedbackCount` read `FeedbackTable` directly, in the shape used by `TestUtility.kt`.

A column absent from the DTO is read from the table. `Feedback` carries no `deviceAgent`, so a test asserting on it reads the column.

`latestBugRowOrNull` and `bugCount` read `BugTable` the same way. They live in `BugUtility.kt` one package up, because `streetlight.server.api` reads them too. `Bug` carries none of the columns a bug test asserts on, so the test-side `BugRow` holds them.

Stating a count alongside the row proves the interaction ran once. A retried click that wrote twice passes every assertion about content.
