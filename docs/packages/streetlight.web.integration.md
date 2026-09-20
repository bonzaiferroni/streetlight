# streetlight.web.integration

Browser tests for the web client. A test here mounts a view into a real DOM, drives it, and asserts on both the DOM and the view model behind it.

`docs/testing.md` states how tests are written. This document states what this package provides.

The tests run inside the browser: the test code compiles to JS and executes in the page alongside the code under test, so a test holds the view model directly. `streetlight.server.e2e` drives a browser from outside and sees only the DOM.

## Running

```
./gradlew :web:jvmTest
```

`ViewSuiteTest` drives the suite. It discovers every test name, then runs each in a fresh page: a new `BrowserContext` loads the generated `test.html` with `--include` set to that one test, and closes when the runner logs `KOTLIN-TEST-FINISHED`. Gradle reports each test by its discovered name.

A test starts with no listeners on `window` or `document`, no history entries and an unmodified URL.

Set `VIEW_HEADED=true` to watch a run in a visible browser.

The browser console is written to the Gradle log as test output when Gradle runs with `-PtestOutput`.

`ViewPageRunner` waits for the finish marker with Playwright's `waitForCondition`, because the Java API delivers page events only while the calling thread is inside a Playwright call. A wait on a plain future never receives the console messages it waits for. A run that times out reports the page errors, the failed requests and the console output.

`ViewPageRunner` serves the generated `dist` directory over HTTP, because `Portal` navigates with `pushState`, which needs a real origin.

`jsBrowserTest` runs the same tests in one shared page. State a test leaves on `window`, `document` or the URL is visible to the tests after it.

The generated `test.html` loads Mocha unpinned, and Mocha 12 breaks the Kotlin test runner's reporter, so no test runs and the run fails with `Timeout 30000ms exceeded`. Both `ViewPageRunner` and the `jsBrowserTest` task rewrite the page to load Mocha 10.8.2. Raise the pin only after confirming the runner works with the newer version.

## Test Names

`ViewPageRunner` selects a test by passing its name to `--include`, which reads `,`, `*` and `!` as syntax. A test name containing any of them cannot be selected, and `ViewSuiteTest` fails naming the test. Write test names without those characters.

Discovery loads the page with Mocha's `dryRun`, and an empty result fails the run.

## ViewTest

`ViewTest` is the base class. It builds a container and a scope before each test and tears both down after.

| Scope | What |
|---|---|
| Once per test | A `MainScope`, an `AppContainer` from `buildTestApp` |
| On teardown | The scope is cancelled and every mounted element is removed |

`mount { }` creates a detached element, attaches it to the document, and mounts the block as a root view. A test mounts the view it is about.

```kotlin
mount { viewFeedbackHub() }
```

Cancelling the scope ends every coroutine the view started, which is the same mechanism that stops work when a view is destroyed in the app.

## buildTestApp

`buildTestApp` returns an `AppContainer` holding the services a mounted view resolves through `app.get<T>()`. It takes each collaborator as a parameter, so a test replaces one and takes the rest.

A view model constructed by a view reaches its dependencies through `ViewScope`, so substituting them in the container substitutes them for the model.

Register a service here when a mounted view resolves it. The container stays small enough to read.

## Driving a View

The helpers in `ViewUtility.kt` act on a mounted `View` through the labels a user sees or the block labels a view declares. A test never reaches into the DOM by structure.

| Helper | What it drives |
|---|---|
| `writeIn`, `editor` | A markdown editor, by block label |
| `chooseIn` | A drop menu, by the block label around it |
| `clickButton`, `button` | A button, by its text |
| `awaitText`, `showsText` | Text anywhere in the view, awaited or checked once |
| `awaitUntil` | Any condition, failing with a description after two seconds |

The editor collapses a whitespace-only line to an empty block, so `writeIn` cannot enter text that is only whitespace.

An action that resolves synchronously is asserted right after it. An action that launches a coroutine is awaited first, because an absence checked early passes for the wrong reason.

A test that needs a collaborator to behave unusually replaces `app` with one from `buildTestApp` before it mounts.

## The Unit Under Test

The unit is a view model together with the view that binds to it. Assertions run in both directions: act on the DOM and read the model, or set the model and read the DOM.

The binding is what needs a browser. A view model tested alone is a unit test.

Leave the shape of a request to the server to `streetlight.server.e2e`.
