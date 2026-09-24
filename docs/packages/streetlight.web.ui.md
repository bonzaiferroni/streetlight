# Package streetlight.web.ui

## Introduction

The browser-side presentation layer. View functions build the DOM through the `koala.dom` DSL and wire it to state exposed by the view models in `streetlight.web.model`.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | The DOM DSL and `ViewScope` |
| `koala.modifier` | Modifiers and classes |
| `streetlight.web.model` | View models |
| `streetlight.web.layouts` | Server-renderable components |
| `streetlight.model` | DTOs and routes |

## Layers

The UI has two layers.

| Layer | Package | Holds |
|---|---|---|
| Declarative UI | `streetlight.web.ui` | Components, and what is rendered from state |
| View model | `streetlight.web.model` | The concept of the UI, its state, and communication with the API |

A view model knows nothing of HTML elements or how they render.

A view is not required to have a view model. `ViewScope` carries the API, and a view whose state is simple declares it and makes its API calls itself. A view gains a view model when its state or its API traffic outgrows that.

## View Functions

A view function is an extension on `ViewScope`, named for what it renders, in a file of the same name.

```kotlin
fun ViewScope.viewFeedbackHub()
```

It takes no context parameter. `ViewScope` carries the `AppContainer`, so a view reaches a service with `app.get<T>()` rather than receiving it through its signature. A new service becomes available to every view at once, and a view's parameters are only what that view is about.

A view rendered directly by a route has a second function taking `RouteScope`, named with a `Route` suffix. The route function reads route arguments and calls the plain view function, so the view itself stays reachable from anywhere.

```kotlin
fun RouteScope.viewFrontDeskRoute() {
    viewFrontDesk()
}
```

Functions prefixed `wire` attach behavior to an element the server already rendered, rather than building their own subtree.

## Scopes

`ViewScope` is the receiver throughout the package. `View` is its implementation and holds the tree: a view registers disposers with `onDispose`, and disposing a view disposes its children in reverse order.

| Scope | Lives until |
|---|---|
| `scope` | The view is disposed |
| `contentScope` | The view's content is cleared or replaced |

Work that rebuilds with the content belongs in `contentScope`. Work that must survive a content rebuild belongs in `scope`. `launchEffect` uses the first and `launchViewEffect` the second.

A view model constructed inside a view is given `contentScope`, so its coroutines end when the content it serves is replaced.

## Rendering State

`flowBlock` renders a value as HTML. It takes a `Tap` or a `Flow`, and its block is rebuilt as a child view whenever the value changes. The previous child view is disposed first.

A state parameter is a `Tap` for a read and a `MutableTap` for a read and write. A `Tap` is a lens onto another `Tap` or onto a `Store`, and a `Store` wraps a `StateFlow`. These types are declared in `kampfire.model`.

## Screens and Routes

`Screen` is an enum in `streetlight.model.ui`. Each entry holds a `RouteParse` that turns a URL into a `StreetlightRoute`, and an optional path root taken from the entry name when absent.

`Portal` (`koala.model`) holds the current route as `PortalState`. It builds the route from the address bar on load and on browser navigation, and it intercepts clicks on local anchors. Code that holds a reference to `Portal` navigates with `Portal.go(route)`.

`viewPortal` is a `flowBlock` over `Portal.screenState`. It renders the selected `Screen` inside a `RouteScope` built from the current `PortalState`, mapping each `Screen` to its route function in a single `when`. A screen with no branch falls to the catch-all rather than failing.

To add a screen: declare the `Screen` entry and its route, write `viewFoo` and `viewFooRoute`, then add the branch in `viewPortal`.

A route whose view reads content has a branch in `AppContentFetcher`. The content type implements `RouteContent`, which `AppContentFetcher` and `routeBlock` both require, whether it is a `FooContent` or a record such as `City`.

## Shell Views

A screen that the server renders on the initial load has a shell in `streetlight.web.shells`. Its view is built from the shell.

```kotlin
fun ViewScope.viewFoo(content: FooContent) {
    shellBox {
        fooShell(content)
    }

    document.setTitle(FooRoute)
    applyTheme(null)
}

fun RouteScope.viewFooRoute() {
    routeBlock<FooRoute, FooContent>(FooShell.IslandId) { content ->
        viewFoo(content)
    }
}
```

`shellBox` adopts the server-rendered shell when it is present and builds the shell in the browser when it is not. The view builds no elements of its own.

A shell view is wired through these parts.

| Part | Location |
|---|---|
| `Api.Content.Foo` endpoint | `streetlight.model.Api` |
| `readFooContent` | `streetlight.server.model`, and bound in `serveContent` |
| `readFooContent` on `ContentClient` | `streetlight.web.io` |
| `is FooRoute` branch | `AppContentFetcher` |
| `Screen.Foo` with `hasShell = true` | `streetlight.model.ui.Screen` |
| `Screen.Foo` branch | `viewPortal` |

A shell view with a map uses `shellBoxWithMap` and sets the marker points from the content.

## Page Body

A routed view without a shell builds its page with `shellBody`, passing its file name, the same way a shell does in `streetlight.web.shells.md`.

```kotlin
fun ViewScope.viewFoo() {
    shellBody("viewFoo.kt") {
        pageHeader("Foo", "a subtitle", SiteImage.Foo)
        // body
    }
}
```

`viewFooConfig` and `viewFooUpdater` views, and screens outside the base layout such as the sandbox, inbox and earth, do not use it.

## Forms

A form is built from the components in `Form.kt`, each a `ViewScope` extension.

| Component | Holds |
|---|---|
| `formCard(name)` | A group of forms shown together, under a heading on a background |
| `formRow` | Form entries in two columns when there is room, and in one column on a narrow screen |
| `formSection(name)` | One form entry. Its heading does most of the work of saying what the field does |
| `centeredText`, `formBullets` | Information a field needs beyond its heading, inside its `formSection` |
| `formSubmit(label, onClick, messenger, back)` | The submit button, its `messageBox`, and an optional back action |

Form entries go in a `formRow`. An entry that deserves the full width on a wide screen, such as a `markdownEditor`, sits in the `formCard` outside any `formRow`.

Closely related fields share one `formSection`.

`formColumn` serves no clear purpose and is not used in new forms.

A field that must be filled calls `flowValid(key, validityTap, contentScope)` on its component, keyed by the edit DTO's `FooProperty` constant, so the field is marked while its key is in the DTO's `validity`.

The form functions for a record live in `FooForm.kt`, one per card, each taking the record's editor. The view stacks them and ends with `formSubmit`.

An editor uploads a pending image with `imageEditor.finalizeImage(messenger)` before it sends the edit.

## Entity Header

`entityHeader` renders the header of an `Entity`, reading its content from the extensions in `streetlight.web.layouts`. A header for content that is not an `Entity` calls `pageHeader` with each value.

A value the entity holds but the header shows differently is a parameter defaulted to the entity's own, as `cells` and `image`.

## Services

`appModule` is the Koin module holding every browser-side service. `AppContainer` wraps the resulting Koin instance and is reached through `ViewScope.app`.

A view model with per-view state is not registered in the module. It is built by a factory extension on `AppContainer` that takes the scope and any starting value, so each view gets its own.

## Delivering UI Messages

Two messengers deliver text to the user. Both implement `Messenger`, so a view model takes either without knowing which it holds.

| Messenger | Holds | Appears |
|---|---|---|
| `MessageStore` | One `UIMessage?`, replaced on each delivery | Wherever the view places its `messageBox` |
| `Toaster` | A list of messages, each expiring on its own | The global toast area |

Prefer a `MessageStore` when the view has room beside the control the user is operating and the declarative UI is within reach. A message next to the button that caused it needs nothing to explain what it refers to.

Reach for the `Toaster` when there is no room beside the control, or when the message reports work the user did not just start.

### The Two Beats

An interaction that reaches the server delivers twice.

| Beat | Says | Delivered by |
|---|---|---|
| First | The interaction had an effect | `messenger.deliverSending()` |
| Second | This was the result | `toDataOr(messenger, defaultOkMessage)` |

`toDataOr` covers both sides of the second beat. On `Ok` it delivers the outcome's own message, or `defaultOkMessage` when the outcome carries none. On `Problem` it delivers the problem message. A flow that wants no success text still reports a failure, so an interaction is never silent about a problem.

Both beats go to the same messenger. A `MessageStore` holds one message, so the second replaces the first in place and the user reads a single slot from "Sending..." to its result.

An interaction whose effect is invisible needs the second beat most. Feedback submitted privately does not appear in the public feed, and without a message the user has no way to tell a successful send from a silent failure.

### Wiring

The view owns the `MessageStore`. It creates one, passes it to `formSubmit` so the box is rendered next to the button, and hands the same instance to the view model when the work happens there.

```kotlin
val messenger = MessageStore()
formSubmit("Send", { model.sendFeedback(messenger) }, messenger)
```

A view model receives the store as a `Messenger` parameter rather than holding one of its own. The view then decides where a message appears and the view model decides only what it says.

## App Overlay

`appOverlay` (`streetlight.web.pages`) is a fixed layer above the route content. `helmBar` sits at its top and the route dock at its bottom. The overlay passes no pointer events, so a child that takes input carries `PointerEventsAuto`.

## Route Dock

The route dock holds the routes directly relevant to the route now. It is rendered once by `viewRouteDock` into `AppOverlay.RouteDockId`, wired from `viewApp`, and persists between routes.

| Part | Holds |
|---|---|
| Title | A label naming what the routes belong to |
| Main routes | Route labels as text |
| Left and right routes | Icons, chosen by `iconOf` |

A side route is for a route that not every view wants, such as a config route. A side route with no entry in `iconOf` fails.

The dock holds routes only. An action belongs in the view it acts on.

A dock route equal to the route now carries `RouteDockStyle.RouteNow`, applied with `flowModifier` over `Portal.routeState`. The match is by route, not screen, because routes of one dock can share a screen. It updates in place, so a dock that persists across routes is not rebuilt to move the indicator.

### State

`RouteDock` in `streetlight.web.model` holds the dock's state, reached through `AppFacade.dock`. On every route it sets its state from `stateOf(route)`, which states everything the route alone determines. A route with no branch clears the dock.

A view adds what only its content knows with `dock.mergeState`, naming its own route. A merge replaces each field it sets and leaves the rest.

```kotlin
dock.mergeState(content.galaxy.toRoute(), RouteDockState(title = content.galaxy.name, rightRoutes = rightRoutes))
```

A merge is applied when the state on hand was built from the same route, and held as pending otherwise, to be applied when that route arrives. The test is the route the state was built from, not the current route in `Portal`, which already reads as the new one while the dock's own reaction is still pending. A pending merge is dropped when a different route arrives.

The route passed is the view's own, never `Portal.stateNow.route`, which would name the route that replaced it.

### Visibility

A view hides the dock with `dock.setVisible(false)`, for content it would otherwise sit over. Every route that lands makes it visible again, so a view states only when to hide it and never when to restore it. The reaction that drives it belongs to the view's `contentScope`, so it ends with the view.

Visibility is not part of `RouteDockState`. That state is the dock's content, handed over once per route, while visibility toggles as often as the view's own content does.

The hidden dock carries `RouteDockStyle.Hidden`, applied with `flowModifier`.

Routes of one group share one `stateOf` branch, or a function it calls, so their docks stay consistent. The universe routes, Home, Galaxies and Cities, share `universeStateOf`, whose right route is the earth route that is the cousin of the route now.
