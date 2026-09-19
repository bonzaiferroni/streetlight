# streetlight.web.integration

Browser tests for the web client. A test here mounts a view into a real DOM, drives it, and asserts on both the DOM and the view model behind it.

`docs/testing.md` states how tests are written. This document states what this package provides.

The tests run inside the browser: the test code compiles to JS and executes in the page alongside the code under test, so a test holds the view model directly. `streetlight.server.e2e` drives a browser from outside and sees only the DOM.

## Running

The Kotlin browser test DSL runs the suite with Playwright driving Chromium and Mocha reporting. Playwright installs the browser on first run. It requires Kotlin 2.4.20 or later.

```
./gradlew :web:jsBrowserTest
```

Set `VIEW_HEADED=true` to watch a run in a visible browser.

The browser console is written to the Gradle log as test output.

The build rewrites the generated `test.html` to load Mocha 10.8.2 before the test task runs. The generated page loads Mocha unpinned, and Mocha 12 breaks the Kotlin test runner's reporter, so no test runs and the task fails with `Timeout 30000ms exceeded`. Raise the pin only after confirming the runner works with the newer version.

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

## The Unit Under Test

The unit is a view model together with the view that binds to it. Assertions run in both directions: act on the DOM and read the model, or set the model and read the DOM.

The binding is what needs a browser. A view model tested alone is a unit test.

Leave the shape of a request to the server to `streetlight.server.e2e`.
